 (ns gyptis-cljremote.letters
   (:require [clojure.java.jdbc :as sql]
             [clj-http.client]))

(def letter-frequency
  [{:letter "A"	:frequency 0.08167 :language "english"}
   {:letter "B"	:frequency 0.01492 :language "english"}
   {:letter "C"	:frequency 0.02782 :language "english"}
   {:letter "D"	:frequency 0.04253 :language "english"}
   {:letter "E"	:frequency 0.12702 :language "english"}
   {:letter "F"	:frequency 0.02288 :language "english"}
   {:letter "G"	:frequency 0.02015 :language "english"}
   {:letter "H"	:frequency 0.06094 :language "english"}
   {:letter "I"	:frequency 0.06966 :language "english"}
   {:letter "J"	:frequency 0.00153 :language "english"}
   {:letter "K"	:frequency 0.00772 :language "english"}
   {:letter "L"	:frequency 0.04025 :language "english"}
   {:letter "M"	:frequency 0.02406 :language "english"}
   {:letter "N"	:frequency 0.06749 :language "english"}
   {:letter "O"	:frequency 0.07507 :language "english"}
   {:letter "P"	:frequency 0.01929 :language "english"}
   {:letter "Q"	:frequency 0.00095 :language "english"}
   {:letter "R"	:frequency 0.05987 :language "english"}
   {:letter "S"	:frequency 0.06327 :language "english"}
   {:letter "T"	:frequency 0.09056 :language "english"}
   {:letter "U"	:frequency 0.02758 :language "english"}
   {:letter "V"	:frequency 0.00978 :language "english"}
   {:letter "W"	:frequency 0.02360 :language "english"}
   {:letter "X"	:frequency 0.00150 :language "english"}
   {:letter "Y"	:frequency 0.01974 :language "english"}
   {:letter "Z"	:frequency 0.00074 :language "english"}
   {:letter "A" :frequency	0.07636 :language "french"}
   {:letter "B" :frequency	0.00901 :language "french"}
   {:letter "C" :frequency	0.03260 :language "french"}
   {:letter "D" :frequency	0.03669 :language "french"}
   {:letter "E" :frequency	0.14715 :language "french"}
   {:letter "F" :frequency	0.01066 :language "french"}
   {:letter "G" :frequency	0.00866 :language "french"}
   {:letter "H" :frequency	0.00737 :language "french"}
   {:letter "I" :frequency	0.07529 :language "french"}
   {:letter "J" :frequency	0.00613 :language "french"}
   {:letter "K" :frequency	0.00049 :language "french"}
   {:letter "L" :frequency	0.05456 :language "french"}
   {:letter "M" :frequency	0.02968 :language "french"}
   {:letter "N" :frequency	0.07095 :language "french"}
   {:letter "O" :frequency	0.05796 :language "french"}
   {:letter "P" :frequency	0.02521 :language "french"}
   {:letter "Q" :frequency	0.01362 :language "french"}
   {:letter "R" :frequency	0.06693 :language "french"}
   {:letter "S" :frequency	0.07948 :language "french"}
   {:letter "T" :frequency	0.07244 :language "french"}
   {:letter "U" :frequency	0.06311 :language "french"}
   {:letter "V" :frequency	0.01838 :language "french"}
   {:letter "W" :frequency	0.00074 :language "french"}
   {:letter "X" :frequency	0.00427 :language "french"}
   {:letter "Y" :frequency	0.00128 :language "french"}
   {:letter "Z" :frequency	0.00326 :language "french"}
   {:letter "A" :frequency	0.06516 :language "german"}
   {:letter "B" :frequency	0.01886 :language "german"}
   {:letter "C" :frequency	0.02732 :language "german"}
   {:letter "D" :frequency	0.05076 :language "german"}
   {:letter "E" :frequency	0.16396 :language "german"}
   {:letter "F" :frequency	0.01656 :language "german"}
   {:letter "G" :frequency	0.03009 :language "german"}
   {:letter "H" :frequency	0.04577 :language "german"}
   {:letter "I" :frequency	0.06550 :language "german"}
   {:letter "J" :frequency	0.00268 :language "german"}
   {:letter "K" :frequency	0.01417 :language "german"}
   {:letter "L" :frequency	0.03437 :language "german"}
   {:letter "M" :frequency	0.02534 :language "german"}
   {:letter "N" :frequency	0.09776 :language "german"}
   {:letter "O" :frequency	0.02594 :language "german"}
   {:letter "P" :frequency	0.00670 :language "german"}
   {:letter "Q" :frequency	0.00018 :language "german"}
   {:letter "R" :frequency	0.07003 :language "german"}
   {:letter "S" :frequency	0.07270 :language "german"}
   {:letter "T" :frequency	0.06154 :language "german"}
   {:letter "U" :frequency	0.04166 :language "german"}
   {:letter "V" :frequency	0.00846 :language "german"}
   {:letter "W" :frequency	0.01921 :language "german"}
   {:letter "X" :frequency	0.00034 :language "german"}
   {:letter "Y" :frequency	0.00039 :language "german"}
   {:letter "Z" :frequency	0.01134 :language "german"}
   {:letter "A" :frequency	0.11525 :language "spanish"}
   {:letter "B" :frequency	0.02215 :language "spanish"}
   {:letter "C" :frequency	0.04019 :language "spanish"}
   {:letter "D" :frequency	0.05010 :language "spanish"}
   {:letter "E" :frequency	0.12181 :language "spanish"}
   {:letter "F" :frequency	0.00692 :language "spanish"}
   {:letter "G" :frequency	0.01768 :language "spanish"}
   {:letter "H" :frequency	0.00703 :language "spanish"}
   {:letter "I" :frequency	0.06247 :language "spanish"}
   {:letter "J" :frequency	0.00493 :language "spanish"}
   {:letter "K" :frequency	0.00011 :language "spanish"}
   {:letter "L" :frequency	0.04967 :language "spanish"}
   {:letter "M" :frequency	0.03157 :language "spanish"}
   {:letter "N" :frequency	0.06712 :language "spanish"}
   {:letter "O" :frequency	0.08683 :language "spanish"}
   {:letter "P" :frequency	0.02510 :language "spanish"}
   {:letter "Q" :frequency	0.00877 :language "spanish"}
   {:letter "R" :frequency	0.06871 :language "spanish"}
   {:letter "S" :frequency	0.07977 :language "spanish"}
   {:letter "T" :frequency	0.04632 :language "spanish"}
   {:letter "U" :frequency	0.02927 :language "spanish"}
   {:letter "V" :frequency	0.01138 :language "spanish"}
   {:letter "W" :frequency	0.00017 :language "spanish"}
   {:letter "X" :frequency	0.00215 :language "spanish"}
   {:letter "Y" :frequency	0.01008 :language "spanish"}
   {:letter "Z" :frequency	0.00467 :language "spanish"}])
(def don-quixote-es
  (slurp "resources/don-quixote.txt"))

 (-> don-quixote-es clojure.string/upper-case set)

(def char-set #{\À \A \¡ \Á \B \C \D \E \F \G \H \I \É \J \K \L \M \Í \N \O \Ï \P \Q \Ñ
                \R \S \Ó \T \U \V \W \X \Y \Ù \Z \Ú \Ü})
(def db
   {
    :classname   "org.h2.Driver"
    :subprotocol "h2:mem"
    :subname     "demo;DB_CLOSE_DELAY=-1"
   }
  )


(defn count-letters
  ([s]
   (count-letters s (map char (range 65 91))))
  ([char-set s]
   (reduce (fn [acc c]
             (if (get acc c)
               (update acc c inc)
               acc))
           (into {} (map (fn [c] [c 0]) char-set))
           (clojure.string/upper-case s))))

 (defn ->frequency
   [letter-count]
   (let [total-chars (reduce + (map second letter-count))]
     (into {} (map (fn [[letter count]] [letter (/ count (double total-chars))]) letter-count))))

(def don-quixote-freq
  (let [freq-map
        (->> don-quixote-es
             (count-letters char-set)
             (->frequency))
        freq-map (select-keys freq-map (map char (range 65 91)))
        ]
    (map (fn [[letter p]]
           {:letter (str letter) :frequency p :language "Mystery 1"})
         freq-map))
  )

(def stuf
  (let [letters-ddl (sql/create-table-ddl :letters
                                          [:letter "CHAR(1)"]
                                          [:frequency "DOUBLE"]
                                          [:language "CHAR(16)"])

        ]
    (try
      (sql/db-do-commands db "DROP TABLE letters;")
      (catch org.h2.jdbc.JdbcBatchUpdateException e))
    (sql/db-do-commands db letters-ddl)
    (apply sql/insert! db :letters letter-frequency)
    (apply sql/insert! db :letters don-quixote-freq)

    ))

#_(def s3-size
  (cloudwatch/get-metric-statistics :metric-name "BucketSizeBytes"
                                    :namespace "AWS/S3"
                                    :start-time (time/date-time 2016 1 1)
                                    :end-time (time/now)
                                    :period 3600
                                    :statistics ["Average"]
                                    :dimensions [#_{:name "BucketName" :value "bto"}]))
#_(def cpu-util
  (amazonica.aws.cloudwatch/get-metric-statistics
    :metric-name "DiskWriteBytes"
    :start-time (time/date-time 2016 1 12)
    :end-time (time/date-time 2016 1 15 20)
    :period 600
    :namespace "AWS/EC2"
    :statistics ["Average" "Maximum"]
    :dimensions [{:name "InstanceId" :value "i-d5a7dc2c"}
                 {:name "InstanceId" :value "i-6064b7e9"}]))
