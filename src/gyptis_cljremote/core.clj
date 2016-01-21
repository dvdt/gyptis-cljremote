(ns gyptis-cljremote.core
  (:require [clojure.core.async :as async]
            [amazonica.aws.ec2 :as ec2]
            [amazonica.aws.cloudwatch :as cloudwatch]
            [clj-time.core :as time]
            [gyptis.core :as gyptis]
            [gyptis.view :as view]
            [gyptis-cljremote.letters :refer [letter-frequency]]))

;; A plot is just data!
(def letter-plot
  (gyptis/dodged-bar letter-frequency
                     {:x :language, :y :frequency :fill :letter}))

(view/plot! "default" (-> letter-plot
                (assoc :height 400)
                (assoc :width 600)))

;;;;;;;;;;;;; based on vega
; => highly customizable, well documented, feature-rich, built for web.
(-> letter-frequency
    #_(gyptis/dodged-bar {:x :language :y :frequency :fill :letter})
    (gyptis/stacked-bar {:x :language :y :frequency :fill :letter})
    (assoc-in [:marks 0 :properties :hover] {:fill {:value "red"}})
    view/plot!)

;;;;;;;;;;;;; templates included for common plot types.
;; easier to comminucate to other people
;; easier to answer questions of your data.
;; lowers learning curve
; bar chart
; line
; choropleth
; facetting / trellis plots

;;;;;;;;;;;;; interactive
; how do i change color scheme?
; how do I change inter-bar spacing?
; how do I make the chart wider?
; add xlabel? ylabel?
(view/plot!
  (-> letter-frequency
      (gyptis/stacked-bar {:x :letter, :y :frequency})
      (assoc-in [:width] 400)
      (assoc-in [:height] 400)
      (assoc-in [:scales 0 :padding] 0.15)
      (assoc-in [:padding :right] 50)
      (assoc-in [:axes 1 :title] "letter frequency")
      (assoc-in [:axes 0 :title] "language")
      (assoc-in [:scales 2 :range] ["red"
                                    "black"])
      (gyptis/facet-grid {:facet_y :language})
      ))
;; Answer a question / tell a data story
;; Guess language of document

;;;;;;;;;;;;;;;;;;;;; how do i...make a dashboard?
;;;;;;;;;; Live updates using a core.async go-loop
(def stop-ch (async/chan))
; Once a second, update the chart
(async/go-loop [data [{:timestamp (time/now)
                       :value (rand)}]]
  (let [[value _] (async/alts! [stop-ch (async/timeout 1000)])]
    (when-not value
      (view/plot! :dashboard
                  (-> data
                      (gyptis/line {:x :timestamp :y :value})))
      (recur (conj data {:timestamp (time/now)
                         :value     (rand)})))))
; Stop updates
(async/go (async/>! stop-ch :stop))


;;;;;;;;;; AWS Dashboard!
;; file:///Users/dtsao/clj/gyptis-cljremote/dashboards/layouts/two-and-one/index.html
(amazonica.core/defcredential {:profile "bto"})

(defonce ph
         (ec2/describe-spot-price-history
           :filters
           [{:name :instance-type :values ["c3.xlarge"
                                           "c3.2xlarge"]}]))
(->> ph :spot-price-history (take 3))

(def price-history-plot
  (let [data (->> ph
                  :spot-price-history
                  (map (fn [x]
                         (update x :spot-price #(Double/parseDouble %))))
                  (sort-by :availability-zone))]
    (-> data
        #_(gyptis/point {:x :timestamp :y :spot-price :fill :instance-type})
        (gyptis/line {:x :timestamp :y :spot-price :stroke :instance-type})
        (gyptis/facet-grid {:facet_x :availability-zone
                            :facet_y :product-description})
        (assoc-in [:width] 800)
        (assoc-in [:padding :right] 200)
        view/plot!)))


(view/plot! "1" (-> letter-plot
                    (gyptis/title "Letters by language")
                    (assoc :width 450)
                    (assoc :height 200)))

(def metrics
  (cloudwatch/list-metrics :namespace "AWS/EC2"
                           :start-time (time/date-time 2016 1 1)))

(def cpu-util-all
  (let [instance-ids (->> metrics
                          :metrics
                          (filter #(= "CPUUtilization" (:metric-name %))))]
    (apply concat
           (pmap
             (fn [{[{instance-id :value}] :dimensions :as args}]
               (->> args (merge {:start-time (time/date-time 2016 1 1)
                                 :end-time   (time/now)
                                 :period     3600
                                 :statistics ["Average"]})
                    (mapcat identity)
                    (apply cloudwatch/get-metric-statistics)
                    :datapoints
                    (map #(assoc % :instance-id instance-id)))) instance-ids))))

(take 3 cpu-util-all)
(let [data (->> cpu-util-all
             (sort-by :timestamp))
      plot-spec
      (-> data
          (gyptis/line {:x :timestamp :y :average :stroke :instance-id})
          (assoc-in [:height] 250)
          (assoc-in [:width] 500)
          (assoc-in [:padding :top] 25)
          (assoc-in [:padding :bottom] 50)
          (assoc-in [:padding :right] 100))]
  (view/plot! "2" plot-spec))
