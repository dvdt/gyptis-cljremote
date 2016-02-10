(ns gyptis-cljremote.core
  (:require [clojure.core.async :as async]
            [clojure.pprint :refer [pprint]]
            [clj-time.core :as time]
            [clojure.java.jdbc :as sql]
            [amazonica.aws.ec2 :as ec2]
            [amazonica.aws.cloudwatch :as cloudwatch]
            [gyptis.core :as gyptis]
            [gyptis.view :as view]
            [gyptis-cljremote.letters :as letters :refer [letter-frequency]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; A plot specification is just data!
(def letter-plot
  (gyptis/stacked-bar [{:letter "A" :frequency 0.08167}
                       {:letter "B" :frequency 0.01492}
                       {:letter "C" :frequency 0.02782}]
                      {:x :letter :y :frequency}))
#_(pprint letter-plot)

(view/plot! "default" letter-plot)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; vega is highly customizable, well documented, feature-rich,
;; built for web.
(-> letter-plot
    ;; change opacity
    #_(assoc-in [:marks 0 :properties :update :fillOpacity :value] 0.5)

    ;; add mouse hover interaction
    #_(assoc-in [:marks 0 :properties :hover] {:fill {:value "red"}})
    view/plot!)

;; how do I change color scheme?
;; how do I change inter-bar spacing?
;; how do I make the chart wider?
;; add xlabel? ylabel?
(view/plot!
  (-> letter-plot
      #_(assoc-in [:width] 400)
      #_(assoc-in [:height] 400)
      #_(assoc-in [:scales 0 :padding] 0.5)
      #_(assoc-in [:axes 1 :title] "letter frequency")
      #_(assoc-in [:axes 0 :title] "language")
      #_(assoc-in [:scales 2 :range] ["red"])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Templates included for common plot types.
;; Easier to comminucate to other people.
;; Eeasier to answer questions of your data.
;; Lowers learning curve by providing defaults.

;; Dodged bars: good for side-by-side comparisons
;; spanish uses 'a' the most; english uses 'y';
;; french uses 'u'; german uses 'n'.
#_(pprint (take 5 letter-frequency))
(view/plot!
  (gyptis/dodged-bar letter-frequency
                     {:x    :letter,
                      :y    :frequency
                      :fill :language}))

;; Stacked bars: good for cumulative comparisons
;; french, spanish and german use non-ascii characters!
(view/plot!
  (gyptis/stacked-bar letter-frequency
                      {:x    :language,
                       :y    :frequency
                       :fill :letter}))

;; Facetting facilitates exploring relationships between
;; many dimensons.
(view/plot!
  (-> letter-frequency
      (gyptis/stacked-bar {:x :letter
                           :y :frequency})
      #_(gyptis/facet-grid {:facet_y :language})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Use SQL to transform data and feed into a plot.
;; What language is the 'Mystery' document?

;; h2 table containing letter frequency data:
(sql/query letters/db ["SELECT * from letters limit 5;"])
(sql/query letters/db ["SELECT language, count(*) as num
                        FROM letters GROUP BY language;"])

(def letter-cross-product
  (sql/query
    letters/db
    ["SELECT a.language as lang_a, a.frequency as freq_a,
             b.language as lang_b, b.frequency as freq_b,
             b.letter as letter
      FROM letters as a, letters as b
      WHERE a.letter=b.letter;"]))

(take 3 letter-cross-product)

(-> letter-cross-product
    (gyptis/point {:x :freq_a :y :freq_b :fill :letter})
    (gyptis/facet-grid {:facet_x :lang_a :facet_y :lang_b})
    (assoc-in [:legends 0 :orient] "left")
    view/plot!
    :legends)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; How do I...make a dashboard?

;; Live updates using a core.async go-loop
(defonce stop-ch (async/chan))
;; Once a second, update the chart with a random number in [0,1)
(async/go-loop [data [{:timestamp (time/now)
                       :value     (rand)}]]
  (let [[value _] (async/alts! [stop-ch (async/timeout 1000)])]
    (when-not value
      (view/plot! "1"
                  (-> data
                      (gyptis/line {:x :timestamp :y :value})
                      (assoc :height 250)
                      (assoc :width 500)))
      (recur (conj data {:timestamp (time/now)
                         :value     (rand)})))))
; Stop updates
#_(async/go (async/>! stop-ch :stop))


;;;;;;;;;; AWS Dashboard!
;; ~/clj/gyptis-cljremote/dashboards/layouts/two-and-one/index.html

;; Set amazon credential -- change to your own aws config
#_(amazonica.core/defcredential {:profile "bto"})

(defn fetch-price-history
  [params]
  (apply ec2/describe-spot-price-history (mapcat identity params)))

(def ph
  (let [params {:start-time (time/from-now (time/weeks -4))
                :end-time   (time/now)
                :filters    [{:name :instance-type :values ["c3.xlarge"
                                                            "c3.2xlarge"]}]}
        ph-1 (fetch-price-history params)
        ph-2 (fetch-price-history (assoc params :next-token (:next-token ph-1)))
        ph-3 (fetch-price-history (assoc params :next-token (:next-token ph-2)))]
    (merge-with concat ph-1 ph-2 ph-3)))



#_(->> ph :spot-price-history (take 3))

(def price-history-plot
  (let [data (->> ph
                  :spot-price-history
                  (map (fn [x]
                         (update x :spot-price #(Double/parseDouble %))))
                  (sort-by :availability-zone))]
    (-> data
        (gyptis/point {:x :timestamp :y :spot-price :fill :instance-type})
        #_(gyptis/line {:x :timestamp :y :spot-price :stroke :instance-type})
        (gyptis.vega-templates/vertical-x-labels)
        (gyptis/facet-grid {:facet_x :availability-zone
                            :facet_y :product-description})
        (assoc-in [:width] 800)
        (assoc-in [:padding :right] 200)
        view/plot!)))

(def metrics
  (cloudwatch/list-metrics :namespace "AWS/EC2"
                           :start-time (time/date-time 2016 1 1)))

(def cpu-util-all-instances
  (let [instance-ids (->> metrics
                          :metrics
                          (filter #(= "CPUUtilization" (:metric-name %))))]
    (apply concat
           (pmap
             (fn [{[{instance-id :value}] :dimensions :as args}]
               (->> args (merge {:start-time (time/date-time 2016 1 1)
                                 :end-time   (time/now)
                                 :period     7200
                                 :statistics ["Average"]})
                    (mapcat identity)
                    (apply cloudwatch/get-metric-statistics)
                    :datapoints
                    (map #(assoc % :instance-id instance-id)))) instance-ids))))

(take 3 cpu-util-all-instances)

(let [data (->> cpu-util-all-instances
                (sort-by :timestamp))
      plot-spec
      (-> data
          (gyptis/line {:x :timestamp :y :average :stroke :instance-id})
           gyptis.vega-templates/vertical-x-labels
          (assoc-in [:height] 250)
          (assoc-in [:width] 500)
          (assoc-in [:padding :top] 25)
          (assoc-in [:padding :bottom] 50)
          (assoc-in [:padding :right] 100))]
  (view/plot! "2" plot-spec))
