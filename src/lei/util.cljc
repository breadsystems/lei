(ns lei.util
  (:require
   [garden.selectors :as s]))

(defn root [m]
  {:pre [(map? m)]}
  [":root" m])

(defn nav>ul
  "Resets for the common `nav > ul` idiom."
  []
  [(s/> :nav :ul) {:list-style :none
                   :padding-left 0}])

(defn pre
  "Resets for pre tag: make its contents wrap and fit inside its container."
  []
  [:pre {:overflow-x :auto
         :white-space :pre-wrap
         :word-wrap :break-word}])