(ns lei.docsite.style
  (:refer-clojure :exclude [* rem])
  (:require
   [garden.def :as ds]
   [garden.selectors :as s]
   [garden.stylesheet :as gs :refer [at-font-face]]
   [garden.units :as u :refer [ch percent px em rem vh vw]]
   [lei.core :as core]
   [lei.docs :as docs]
   [lei.resets :as resets]
   [lei.util :as util :refer [defutil]]))

(s/defselector *)

(ds/defcssfn url)

(def translucent-rose "#ffe9edbf")
(def dark-purple "#781757")
(def light-grey "#e9e9e9")
(def green "rgb(7, 128, 7)")

(defutil margin-top:0)
(defutil text-align:center)
(defutil text:small {:font-size (em 0.8)})

(def screen
  [(at-font-face {:font-family "FiraCode"
                  :src (url "https://unpkg.com/firacode/distr/woff2/FiraCode-Regular.woff")})
   ;; Global defaults
   resets/box-sizing
   resets/zero-margin
   resets/list-style-none
   resets/body
   resets/wrap-pre
   resets/motion
   (core/axioms)
   (core/modular-scale :font-size (em 3.5) :h2 :h3 :h4 :h5)

   ;; General layout stuff
   (core/stack {:selector :.big-stack
                :space (em 6)})
   (core/stack {:selector :.stack
                :recursive? true
                :space (em 1.5)})
   (core/sidebar {:space (rem 1)
                  :content-min-width (percent 60)
                  :sidebar-width (ch 30)})
   [:header {:min-height (vh 50)}
    [:h1 {:margin-top (vh 7)
          :font-size (rem 7)}]
    [:h2 {:font-size (rem 3)}]]
   [(s/> :nav :ul) {:margin-top (em 0.5)}
    [:li {:margin-top (em 0.3)}]
    [(s/> :li :ul) {:padding-left (em 1)
                    :font-size (em 0.8)}]]
   [(s/& :nav (s/attr :role=navigation)) {:margin-top (em 1)}]

   ;; Misc
   [:h1 [:&:after {:content "'🌺'"
                   :display :inline-block
                   :margin-left (rem 1)
                   :font-style :normal}]]

   ;; Fonts and colors
   [:body {:background-color translucent-rose
           :font-size (px 20)
           :margin-top 0
           :padding-top 0
           :padding-left (rem 2)
           :padding-right (rem 2)
           :padding-bottom (rem 10)}]
   [* {:font-family (cons "Heebo" util/system-sans)}]
   [:h1 :h2 {:font-family "Playfair Display, sans-serif"}]
   [:h1 :h2 :h3 :h4 :h5 :h6 {:color green}]
   [:header
    [* {:text-align :center
        :font-family "Playfair Display, sans-serif"
        :font-weight 700}]]
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
   [(s/& :code (s/not "[class*='hljs']")) {:background light-grey
                                            :padding "0.1em 0.3em"
                                            :border "1px solid #e6c7c7"
                                            :border-radius (em 0.2)
                                            :color dark-purple}]
   [:code
    [:& * {:min-width (ch 70)
           :font-family ["FiraCode" "monospace"]}]]

   ;; Utility classes
   margin-top:0
   text-align:center
   text:small])