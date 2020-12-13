;; Based on to Andy Bell's Modern CSS Reset
;; https://hankchizljaw.com/wrote/a-modern-css-reset/
(ns lei.resets
  (:require
   [garden.core]
   [garden.stylesheet :refer [at-media]]))

(def box-sizing
  [":*,*::before,*::after" {:box-sizing :border-box}])

(def zero-margin
  [:body :h1 :h2 :h3 :h4 :p :figure :blockquote :dl :dd {:margin 0}])

(def list-style-none
  ["ul[role=list],ol[role=list]" {:list-style :none}])

(def body
  [:body {:min-height "100vh"
          :text-rendering :optimizeSpeed
          :line-height 1.5}])

(def a-skip-ink
  ["a:not([class])" {:text-decoration-skip-ink :auto}])

(def images
  ["img,picture" {:max-width "100%" :display :block}])

(def input-font
  ["input,button,textarea,select" {:font :inherit}])

(def wrap-pre
  [:pre {:overflow-x :auto
         :white-space :pre-wrap
         :word-wrap :break-word}])

(def motion
  [[:html {:scroll-behavior :smooth}]
   (at-media {:prefers-reduced-motion :reduce}
             ["*,*::before,*::after"
              {:animation-duration "0.01ms !important"
               :animation-iteration-count "1 !important"
               :transition-duration "0.01ms !important"
               :scroll-behavior "auto !important"}])])

(def all
  [box-sizing
   zero-margin
   list-style-none
   body
   a-skip-ink
   images
   input-font
   wrap-pre
   motion])