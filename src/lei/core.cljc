(ns lei.core
  (:refer-clojure :exclude [> * rem])
  (:require
   #?(:clj
      [clojure.string :refer [join]])
   [garden.arithmetic :as ga]
   [garden.core :as garden]
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

(comment
  (calc (u/percent 100) :- (u/rem 1))
  (calc (u/em 5) :+ (u/px 5))
  (calc nil))



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
     [[* {:box-sizing :content-box
          :max-width (ch measure)}]
      (conj measure-exceptions {:max-width :none})]
     (wrapper-axiom args))))

(comment
  (garden/css (axioms))
  (garden/css (axioms {:measure 50})))

(defn modular-scale [opts prop initial & selectors]
  (let [[opts prop initial selectors]
        (if (map? opts)
          [opts prop initial selectors]
          [{} opts prop (cons initial selectors)])
        {:keys [ratio op]} (merge {:ratio 1.618 :op ga//} opts)]
    (vec (loop [rules []
                  measurement initial
                  [selector & rest] selectors]
             (if (nil? selector)
               rules
               (recur (conj rules [selector {prop measurement}])
                      (op measurement 1 ratio)
                      rest))))))

(defn
  ^{:lei/name "Stack"
    :lei/description "Two or more vertically stacked elements."
    :lei/options
    [{:name :selector
      :desc "The selector for the top-level Stack element."
      :default :.stack}
     {:name :recursive?
      :desc "Whether to apply spacing (vertical margins) recursively."
      :default nil}]
    :lei/examples
    [{:name "Default"
      :form '(lei.core/stack)
      :desc "You can pass nothing (or an empty map) for a default Stack."}
     {:name "Custom selector"
      :form '(lei.core/stack {:selector :main})
      :desc "Specify a custom selector to target the top-level Stack
             element. Can be any valid Garden selector."}
     {:name "Recursion"
      :form '(lei.core/stack {:recursive? true})
      :desc "Makes the Stack recursive, targetting all series of
             two or more elements inside rather than only immediate
             children of `selector`."}]}
  stack
  ([]
   (stack {}))
  ([{:keys [selector recursive?]}]
   (let [selector (or selector :.stack)
         stack-rule [selector {:display :flex
                               :flex-direction :column
                               :justify-content :flex-start}]
         ;; Nested rules are the same irrespective of recursion;
         ;; only the selectors differ.
         nest (fn [sel rules]
                (if recursive?
                  ;; regular owl: .stack * + *
                  [selector [sel rules]]
                  ;; caret plus owl: .stack > * + *
                  [(s/> selector sel) rules]))
         child-rule (nest * {:margin-top 0
                             :margin-bottom 0})
         grandchild-rule (nest (s/+ * *) {:margin-top (rem 1.5)})]
     [stack-rule child-rule grandchild-rule])))

(comment
  (garden/css (stack))
  (garden/css (stack {:recursive? true}))
  (garden/css (stack {:selector :.my-stack}))
  (garden/css (stack {:selector :.stackalicious :recursive? true})))

(defn
  ^{:lei/name "Sidebar"
    :lei/description 
  "Rules for styling the Sidebar pattern. Assumes exactly two children of the
   intermediate flexbox wrapper element. Takes the following options:
   * container - the selector for the wrapper element. Default: :.with-sidebar
   * content-min-width - the minimum width, in any unit, for the content element.
     Required.
   * sidebar-width - the width of the sidebar, in any unit. Declared as flex-basis
     in the resulting CSS.
   * space - the horizontal space, in any unit, between sidebar and content.
     Default: 1rem"
    }
  sidebar
  [{:keys [container content-min-width sidebar-width space]}]
  {:pre [(u/unit? content-min-width)
         (or (nil? container) (s/selector? container))
         (or (nil? sidebar-width) (u/unit? sidebar-width))
         (or (nil? space) (u/unit? space))]}
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

  (comment
    (garden/css (sidebar {:container :.wrapper}))
    (garden/css (sidebar {:container :main}))
    (garden/css (sidebar {:content-min-width (u/px 500)}))
    (garden/css (sidebar {:sidebar-width (u/px 100)}))
    (garden/css (sidebar {:space (u/rem 1.5)})))