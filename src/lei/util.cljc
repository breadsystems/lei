(ns lei.util
  (:refer-clojure :exclude [*])
  (:require
   [garden.selectors :as s]))

(s/defselector *)

(defn root [m]
  {:pre [(map? m)]}
  [":root" m])

(defn nav>ul
  "Resets for the common `nav > ul` idiom."
  []
  [(s/> :nav :ul) {:list-style :none
                   :padding-left 0}])

(defn line-height
  "Defaults for body text line-height."
  []
  [:p :li :blockquote {:line-height 1.5}])

(defn pre
  "Resets for pre tag: make its contents wrap and fit inside its container."
  []
  [:pre {:overflow-x :auto
         :white-space :pre-wrap
         :word-wrap :break-word}])

(def system-serif
  ["Georgia" "Times" "Times New Roman" "serif"])

(def system-sans
  ["-apple-system"
   "BlinkMacSystemFont"
   "Segoe UI"
   "Roboto"
   "Oxygen-Sans"
   "Ubuntu"
   "Cantarell"
   "Helvetica Neue"
   "sans-serif"])