(ns lei.core-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest are]]
   [garden.arithmetic :as ga]
   [garden.core :as garden]
   [garden.selectors :as sel]
   [garden.units :as u]
   [lei.core :as core]))

(deftest test-measure-axiom
  (are [x y] (= (str/split (garden/css x) #"\n")
                (str/split (garden/css y) #"\n"))

    ;; Defaults
    [[:* {:max-width (u/ch 80)}]
     [:html :body :div :header :nav :main :footer {:max-width :none}]]
    (core/measure-axiom)

    ;; Defaults passing empty map
    [[:* {:max-width (u/ch 80)}]
     [:html :body :div :header :nav :main :footer {:max-width :none}]]
    (core/measure-axiom {})

    ;; Custom measure
    [[:* {:max-width (u/ch 75)}]
     [:html :body :div :header :nav :main :footer {:max-width :none}]]
    (core/measure-axiom {:measure 75})
    
    ;; Custom exceptions
    [[:* {:max-width (u/ch 80)}]
     [:html :.custom {:max-width :none}]]
    (core/measure-axiom {:measure-exceptions [:html :.custom]})))

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
     [:h5 {:font-size (u/em (* 1.5))}]
     [:h4 {:font-size (u/em (* 1.5 1.5))}]
     [:h3 {:font-size (u/em (* 1.5 1.5 1.5))}]
     [:h2 {:font-size (u/em (* 1.5 1.5 1.5 1.5))}]
     [:h1 {:font-size (u/em (* 1.5 1.5 1.5 1.5 1.5))}]]
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
    (core/stack {:selector :.special})

    ;; Using custom spacing, non-recursive
    [[:.stack {:display :flex
               :flex-direction :column
               :justify-content :flex-start}]
     [(sel/> :.stack :*) {:margin-top 0
                          :margin-bottom 0}]
     [(sel/> :.stack (sel/+ :* :*)) {:margin-top (u/em 2)}]]
    (core/stack {:space (u/em 2)})

    ;; Using default class, recursive
    [[:.stack {:display :flex
               :flex-direction :column
               :justify-content :flex-start}
      [:* {:margin-top 0
           :margin-bottom 0}]
      [(sel/+ :* :*) {:margin-top (u/rem 1.5)}]]]
    (core/stack {:recursive? true})

    ;; Define a stack exception
    [[:.stack {:display :flex
               :flex-direction :column
               :justify-content :flex-start}
      [:.stack-exception {:margin-top (u/rem 3)}]]
     [(sel/> :.stack :*) {:margin-top 0
                          :margin-bottom 0}]
     [(sel/> :.stack (sel/+ :* :*)) {:margin-top (u/rem 1.5)}]]
    (core/stack {:exception [:.stack-exception {:margin-top (u/rem 3)}]})))

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

(deftest test-center
  (are [x y] (= (str/split (garden/css x) #"\n")
                (str/split (garden/css y) #"\n"))

    ;; Using defaults
    [[:.center {:box-sizing :content-box
                :margin-left :auto
                :margin-right :auto
                :max-width (u/ch 80)}]]
    (core/center)

    ;; Using defaults, passing an empty map
    [[:.center {:box-sizing :content-box
                :margin-left :auto
                :margin-right :auto
                :max-width (u/ch 80)}]]
    (core/center {})

    ;; Using a custom max-width
    [[:.center {:box-sizing :content-box
                :margin-left :auto
                :margin-right :auto
                :max-width (u/ch 25)}]]
    (core/center {:max-width (u/ch 25)})

    ;; Using a custom selector
    [[:.my-center {:box-sizing :content-box
                   :margin-left :auto
                   :margin-right :auto
                   :max-width (u/ch 80)}]]
    (core/center {:selector :.my-center})

    ;; With gutters
    [[:.center {:box-sizing :content-box
                :margin-left :auto
                :margin-right :auto
                :max-width (u/ch 80)
                :padding-left (u/em 1)
                :padding-right (u/em 1)}]]
    (core/center {:gutter (u/em 1)})

    ;; Aligning text
    [[:.center {:box-sizing :content-box
                :margin-left :auto
                :margin-right :auto
                :max-width (u/ch 80)
                :text-align :center}]]
    (core/center {:align-text? true})

    ;; With intrinsic centering
    [[:.center {:box-sizing :content-box
                :margin-left :auto
                :margin-right :auto
                :max-width (u/ch 80)
                :display :flex
                :flex-direction :column
                :align-items :center}]]
    (core/center {:intrinsic? true})

    ;; Composing all options
    [[:.custom-center {:box-sizing :content-box
                       :margin-left :auto
                       :margin-right :auto
                       :max-width (u/ch 50)
                       :text-align :center
                       :padding-left (u/em 1.5)
                       :padding-right (u/em 1.5)
                       :display :flex
                       :flex-direction :column
                       :align-items :center}]]
    (core/center {:selector :.custom-center
                  :max-width (u/ch 50)
                  :align-text? true
                  :gutter (u/em 1.5)
                  :intrinsic? true})))