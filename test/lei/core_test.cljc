(ns lei.core-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest are]]
   [garden.arithmetic :as ga]
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

(deftest test-modular-scale
  (are [css args]
       (= css (apply core/modular-scale args))

    ;; Default ratio, default op
    [[:h1 {:font-size (u/em 20)}]
     [:h2 {:font-size (u/em (/ 20 1.618))}]
     [:h3 {:font-size (u/em (/ 20 1.618 1.618))}]
     [:h4 {:font-size (u/em (/ 20 1.618 1.618 1.618))}]
     [:h5 {:font-size (u/em (/ 20 1.618 1.618 1.618 1.618))}]
     [:h6 {:font-size (u/em (/ 20 1.618 1.618 1.618 1.618 1.618))}]]
    [:font-size (u/em 20) :h1 :h2 :h3 :h4 :h5 :h6]

    ;; Ratio of 1.5
    [[:h1 {:font-size (u/em 20)}]
     [:h2 {:font-size (u/em (/ 20 1.5))}]
     [:h3 {:font-size (u/em (/ 20 1.5 1.5))}]
     [:h4 {:font-size (u/em (/ 20 1.5 1.5 1.5))}]
     [:h5 {:font-size (u/em (/ 20 1.5 1.5 1.5 1.5))}]
     [:h6 {:font-size (u/em (/ 20 1.5 1.5 1.5 1.5 1.5))}]]
    [{:ratio 1.5} :font-size (u/em 20) :h1 :h2 :h3 :h4 :h5 :h6]

    ;; Ratio of 1.5, op of ga/*
    [[:h6 {:font-size (u/em 1)}]
     [:h5 {:font-size (u/em (* 1 1.5))}]
     [:h4 {:font-size (u/em (* 1 1.5 1.5))}]
     [:h3 {:font-size (u/em (* 1 1.5 1.5 1.5))}]
     [:h2 {:font-size (u/em (* 1 1.5 1.5 1.5 1.5))}]
     [:h1 {:font-size (u/em (* 1 1.5 1.5 1.5 1.5 1.5))}]]
    [{:ratio 1.5 :op ga/*} :font-size (u/em 1) :h6 :h5 :h4 :h3 :h2 :h1]))

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

    ;; Same as above, but passing an empty map
    [[:.stack {:display :flex
               :flex-direction :column
               :justify-content :flex-start}]
     [(sel/> :.stack :*) {:margin-top 0
                          :margin-bottom 0}]
     [(sel/> :.stack (sel/+ :* :*)) {:margin-top (u/rem 1.5)}]]
    (core/stack {})

    ;; Using a custom class, non-recursive
    [[:.special {:display :flex
                 :flex-direction :column
                 :justify-content :flex-start}]
     [(sel/> :.special :*) {:margin-top 0
                            :margin-bottom 0}]
     [(sel/> :.special (sel/+ :* :*)) {:margin-top (u/rem 1.5)}]]
    (core/stack {:class :.special})

    ;; Using default class, recursive
    [[:.stack {:display :flex
               :flex-direction :column
               :justify-content :flex-start}
      [:* {:margin-top 0
           :margin-bottom 0}]
      [(sel/+ :* :*) {:margin-top (u/rem 1.5)}]]]
    (core/stack {:recursive? true})))

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