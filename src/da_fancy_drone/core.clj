(ns da-fancy-drone.core
  (:use [clojure.core.async])
  (:require [flightgear.api :as fg]
           [clojure.pprint :as pprint]
           [flightgear.core :as fgc]))

(defn start
  []
  (fg/starter! 1)
  (fg/flaps! 0.5)
  (fg/throttle! 1)
  (fg/aileron! 0)
  (fg/rudder! 0))

(defn aircraft-state []
  {:airspeed (fg/indicated-airspeed-kt)
   :altitude (fg/indicated-altitude-ft)
   :attitude (fg/indicated-attitude)
   :position (fg/position)
   :orientation (fg/orientation)
   :heading (fg/indicated-heading-deg)})

(defn print-properties
  []
  (pprint/pprint (aircraft-state)))

(defn print-controls
  []
  (pprint/pprint {:rudder (fgc/request-property-list "/controls/flight/rudder")}))

(defn read-state [c]
  (thread (while true (do (>!! c (aircraft-state))
                          ))))


(defn print-airspeed [c]
  (go (while true (println "airspeed" (:airspeed (<! c))))))

(defn print-heading [c]
  (go (while true (println "heading" (:heading (<! c))))))


(defn -main []
  (fg/connect "localhost" 5401)
  (start)
  (let [c (chan)
        c-mult (mult c)
        airspeed-ch (tap c-mult (chan))
        heading-ch (tap c-mult (chan))
        ]
    (read-state c)
    (print-airspeed airspeed-ch)
    (print-heading heading-ch))
  (Thread/sleep 100000))
  ;[]
  ;
  ;(start)
  ;(print-properties)
  ;(print-controls))