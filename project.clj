(defproject gyptis-cljremote "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [com.h2database/h2 "1.4.190"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [clj-http "2.0.0"]
                 [gyptis "0.2.2" :exclusions [joda-time
                                              com.fasterxml.jackson.core/jackson-core]]
                 [amazonica "0.3.47"]])
