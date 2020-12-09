(ns lei.core-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest are]]
   [garden.core :as garden]
   [garden.selectors :as sel]
   [garden.units :as u]
   [lei.core :as core]))

(deftest test-wrapper-axiom
  (are [x y] (= (str/split (garden/css x) #"\n")
                (str/split (garden/css y) #"\n"))

    ;; Defaults
    [:body {:max-width (u/rem 80)
            :margin-left :auto
            :margin-right :auto}]
    (core/wrapper-axiom)

    ;; No wrapper
    nil
    (core/wrapper-axiom {:wrapper false})

    ;; Custom width
    [:body {:max-width (u/rem 100)
            :margin-left :auto
            :margin-right :auto}]
    (core/wrapper-axiom {:wrapper-width (u/rem 100)})

    ;; Custom selectory & width
    [:main {:max-width (u/rem 100)
            :margin-left :auto
            :margin-right :auto}]
    (core/wrapper-axiom {:wrapper :main
                         :wrapper-width (u/rem 100)})

    ;; Without centering (only apply max-width)
    [:body {:max-width (u/rem 80)}]
    (core/wrapper-axiom {:center? false})))

(deftest test-stack
  (are [x y] (= (str/split (garden/css x) #"\n")
                (str/split (garden/css y) #"\n"))

    ;; Using default class, non-recursive
    [[:.stack {:display :flex
               :flex-direction :column
               :justify-content :flex-start}]
     [(sel/> :.stack :*) {:margin-top 0
                          :margin-bottom 0}]
     [(sel/> :.stack (sel/+ :* :*)) {:margin-top (u/rem 1.5)}]]
    (core/stack)

    ;; Using a custom class, non-recursive
    [[:.special {:display :flex
                 :flex-direction :column
                 :justify-content :flex-start}]
     [(sel/> :.special :*) {:margin-top 0
                            :margin-bottom 0}]
     [(sel/> :.special (sel/+ :* :*)) {:margin-top (u/rem 1.5)}]]
    (core/stack {:class :.special})))

(comment
  (garden/css [:.x [(sel/+ :* :*) {:foo :bar}]])
  (garden/css [(sel/> :.x (sel/+ :* :*)) {:foo :bar}]))

(deftest test-sidebar
  (are [x y] (= (str/split (garden/css x) #"\n")
                (str/split (garden/css y) #"\n"))

    ;; using default spacing
    [[(sel/> :.with-sidebar :*) {:display :flex
                                 :flex-wrap :wrap
                                 :margin (u/rem -0.5)}]
     [(sel/> :.with-sidebar :* :*) {:margin (u/rem 0.5)}
      [:&:first-child {:flex-basis (u/ch 25)
                       :flex-grow 1}]
      [:&:last-child {:flex-basis 0
                      :flex-grow 999
                      :min-width "calc(75% - 1rem)"}]]]
    (core/sidebar {:content-min-width (u/percent 75)
                   :sidebar-width (u/ch 25)})

    ;; using custom spacing and widths
    [[(sel/> :.with-sidebar :*) {:display :flex
                                 :flex-wrap :wrap
                                 :margin (u/rem -1.0)}]
     [(sel/> :.with-sidebar :* :*) {:margin (u/rem 1.0)}
      [:&:first-child {:flex-basis (u/ch 10)
                       :flex-grow 1}]
      [:&:last-child {:flex-basis 0
                      :flex-grow 999
                      :min-width "calc(60% - 2rem)"}]]]
    (core/sidebar {:content-min-width (u/percent 60)
                   :sidebar-width (u/ch 10)
                   :space (u/rem 2)})

    ;; using custom wrapper class
    [[(sel/> :.party-time :*) {:display :flex
                               :flex-wrap :wrap
                               :margin (u/rem -1.0)}]
     [(sel/> :.party-time :* :*) {:margin (u/rem 1.0)}
      [:&:first-child {:flex-basis (u/ch 10)
                       :flex-grow 1}]
      [:&:last-child {:flex-basis 0
                      :flex-grow 999
                      :min-width "calc(60% - 2rem)"}]]]
    (core/sidebar {:container :.party-time
                   :content-min-width (u/percent 60)
                   :sidebar-width (u/ch 10)
                   :space (u/rem 2)})))