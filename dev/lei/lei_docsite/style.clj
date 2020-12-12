(ns lei.lei-docsite.style
  (:refer-clojure :exclude [rem])
  (:require
   [garden.selectors :as s]
   [garden.units :as u :refer [ch percent px em rem]]
   [lei.core :as core]
   [lei.util :as util]))

(def translucent-rose "#ffe9ede3")
(def green "rgb(8, 142, 8)")

(def screen
  [(core/axioms)
   (core/modular-scale :font-size (em 3.5) :h1 :h2 :h3 :h4 :h5)
   ;; General layout stuff
   (core/stack {:recursive? true})
   (core/sidebar {:space (rem 1)
                  :content-min-width (percent 60)
                  :sidebar-width (ch 20)})
   ;; Spacing
   (util/line-height)
   ;; Fonts and colors
   [:body {:background-color translucent-rose
           :font-size (px 18)
           :padding (rem 1)}]
   [:* {:font-family util/system-sans}]
   [:h1 :h2 :h3 :h4 :h5 :h6 {:color green
                             :font-family util/system-serif}]
   [:h2 :h3 :h4 :h5 :h6 {:margin-top 0}]
   [:h1
    [:&:after {:content "'🌺'"
               :display :inline-block
               :margin-left (rem 1)}]]
   [(s/> :header :h2) {:font-style :italic}]
   [:a {:text-decoration :none
        :font-weight 700
        :color green}]
   ;; Misc
   [:header {:text-align :center}]
   (util/nav>ul)
   (util/pre)])