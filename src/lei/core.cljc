(ns lei.core
  (:refer-clojure :exclude [> * rem])
  (:require
   #?(:clj
      [clojure.string :refer [join]])
   [garden.arithmetic :as ga]
   [garden.core]
   [garden.selectors :as s :refer [defselector >]]
   [garden.units :as u :refer [ch rem]]))

(defselector *)



    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ;;                          ;;
  ;;         Helpers          ;;
 ;;                          ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- calc-str [x]
  (cond
    (u/unit? x) (str (.magnitude x) (name (.unit x)))
    (keyword? x) (name x)
    (symbol? x) (name x)
    :else (str x)))

;; TODO require-macros in cljs
#?(:clj
   (defmacro calc [& args]
     `(str "calc(" (join " " (map calc-str ~(vec args))) ")")))



    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ;;                          ;;
  ;;          Axioms          ;;
 ;;                          ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn wrapper-axiom
  ([]
   (wrapper-axiom {}))
  ([{:keys [wrapper wrapper-width center?]}]
   (let [selector (if (false? wrapper)
                    false
                    (or wrapper :body))
         width (or wrapper-width (rem 80))
         margin-rules (when-not (false? center?)
                        {:margin-left :auto
                         :margin-right :auto})]
     [selector (conj {:max-width width} margin-rules)])))

(defn axioms
  ([]
   (axioms {}))
  ([{:keys [measure measure-exceptions] :as args}]
   (let [measure (or measure 80)
         measure-exceptions (or measure-exceptions
                                [:html :body :div :header :nav :main :footer])]
     [[:* {:box-sizing :content-box
           :max-width (ch measure)}]
      (conj measure-exceptions {:max-width :none})]
     (wrapper-axiom args))))

(comment
  (garden.core/css (axioms))
  (garden.core/css (axioms {:measure 50})))

(defn stack
  ([]
   (stack {:class :.stack}))
  ([{:keys [class]}]
   [[class {:display :flex
            :flex-direction :column
            :justify-content :flex-start}]
    [(s/> class :*) {:margin-top 0
                     :margin-bottom 0}]
    [(s/> class (s/+ * *)) {:margin-top (rem 1.5)}]]))

(defn sidebar [{:keys [container content-min-width sidebar-width space]}]
  (let [container (or container :.with-sidebar)
        space (or space (rem 1))
        margin (ga/* space 0.5)]
    [[(> container *) {:display :flex
                   :flex-wrap :wrap
                   :margin (ga/- margin)}]
     [(> container * *) {:margin margin}
      [:&:first-child {:flex-basis sidebar-width
                       :flex-grow 1}]
      [:&:last-child {:flex-basis 0
                      :flex-grow 999
                      :min-width (calc content-min-width :- space)}]]]))