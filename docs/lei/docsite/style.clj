(ns lei.docsite.style
  (:refer-clojure :exclude [* rem])
  (:require
   [garden.def :as ds]
   [garden.selectors :as s]
   [garden.stylesheet :as gs :refer [at-font-face]]
   [garden.units :as u :refer [ch percent px em rem]]
   [lei.core :as core]
   [lei.docs :as docs]
   [lei.resets :as resets]
   [lei.util :as util :refer [defutil]]))

(s/defselector *)

(ds/defcssfn url)

(def translucent-rose "#ffe9edbf")
(def green "rgb(7, 128, 7)")

(defutil text-align:center)
(defutil text:small {:font-size (em 0.8)})

(def screen
  [(at-font-face {:font-family "FiraCode"
                  :src (url "https://unpkg.com/firacode/distr/woff2/FiraCode-Regular.woff")})
   ;; Global defaults
   resets/box-sizing
   resets/list-style-none
   resets/body
   resets/wrap-pre
   resets/motion
   (core/axioms)
   (core/modular-scale :font-size (em 3.5) :h1 :h2 :h3 :h4 :h5)

   ;; General layout stuff
   (core/stack {:recursive? true})
   (core/sidebar {:space (rem 1)
                  :content-min-width (percent 60)
                  :sidebar-width (ch 30)})
   ;; Align nav ul with top of main content
   [:nav [:ul {:margin-top (em 0.5)}]]

   ;; Misc
   [:h1 [:&:after {:content "'🌺'"
                   :display :inline-block
                   :margin-left (rem 1)
                   :font-style :normal}]]

   ;; Fonts and colors
   [:body {:background-color translucent-rose
           :font-size (px 18)
           :padding-left (rem 2)
           :padding-right (rem 2)
           :padding-bottom (rem 10)}]
   [* {:font-family util/system-sans}]
   [:h1 :h2 :h3 :h4 :h5 :h6 {:color green}]
   [:header [* {:text-align :center
                :font-family util/system-serif
                :font-style :italic}]]
   [:a {:text-decoration :none
        :font-weight 700
        :color green}]

   ;; Code
   (docs/code-example-styles
    {:default-label-styles {:color :black
                            :font-weight 700}
     :selected-label-styles {:background :pink
                             :text-decoration-color :darkmagenta
                             :text-decoration-style :dotted}})
   [:code [:& * {:font-family ["FiraCode" "monospace"]}]]

   ;; Utility classes
   text-align:center
   text:small])