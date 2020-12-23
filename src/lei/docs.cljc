(ns lei.docs
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [clojure.walk :as walk]
   [garden.core :as garden]
   [garden.selectors]
   [markdown.core :as md]))

(defprotocol GardenRenderable
  (as-garden [this]))

(extend-protocol GardenRenderable
  java.lang.Object
  (as-garden [this]
    this)

  garden.types.CSSUnit
  (as-garden [this]
    (list (symbol (str "garden.units/" (symbol (.-unit this))))
          (.-magnitude this))))

(defn path->html [path]
  (-> (io/resource path) slurp md/md-to-html-string))

(defn dangerous [tag attrs & [html]]
  (let [[attrs html] (if (map? attrs )
                       [attrs html]
                       [{} attrs])]
    [tag (merge attrs {:dangerouslySetInnerHTML {:__html html}})]))

(defn slug [& ss]
  (if ss
    (str/lower-case
     (str/join "-" (map (comp str #(str/replace % #" " "-")) ss)))
    ""))

(defn anchor [& ss]
  (if ss
    (str "#" (apply slug ss))
    ""))

(defn section-heading [tag & sections]
  [:<>
   [:a {:name (apply slug sections)}]
   [:a {:href (apply anchor sections)} [tag (last sections)]]])

(defmulti snippet :lei/renderer)

(defmethod snippet :default [form]
  ;; TODO code formatting
  [:pre (str form)])

(defmulti result :lei/renderer)

(defmethod result :default [form]
  [:pre (str (walk/postwalk as-garden (eval form)))])

(defmulti pattern :lei/renderer)

(defmethod pattern :default [data]
  (let [{:lei/keys [name description options examples]
         :keys [doc file line]}
        data
        section-name name]
    [:article
     (section-heading :h2 section-name)
     ;; TODO link to source line in VCS
     (when (and file line)
       [:p.text:small [:code (str file ":" line)]])
     [:p (str (or description doc))]
     (when examples
       [:section
        (section-heading :h3 name "Examples")
        (for [{:keys [name form description]} examples]
          [:div
           (section-heading :h4 section-name "examples" name)
           (dangerous :div (md/md-to-html-string description))
           (snippet form)
           [:p "Result:"]
           (result form)])])
     (when options
       [:section
        (section-heading :h3 name "Options")
        (for [{:keys [name description default required?]} options]
          [:div
           (section-heading :h4 section-name "options" name)
           [:p
            (when required? [:strong "Required. "])
            (dangerous :div (md/md-to-html-string description))
            (when default [:span " Default: " [:code default]])]])])]))

(defn var->map [v]
  (let [m (meta v)]
    {:name (:lei/name m)
     :content (pattern m)}))

(defmulti page :lei/renderer)

(defmethod page :default [{:keys [title
                                  description
                                  styles
                                  heading
                                  subheading
                                  sections]}]
  [:html {:lang "en-US"}
   [:head
    [:meta {:charset "utf-8"}]
    [:title title]
    (when-let [metadesc (or description subheading)]
      [:meta {:name "description"
              :content metadesc}])
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]
    (dangerous :style (garden/css styles))]
   [:body
    [:header
     [:h1 (or heading title)]
     (when subheading [:h2 subheading])]
    [:div.with-sidebar
     [:div
      ;; Sidebar
      [:nav {:role :navigation}
       [:ul {:role :list}
        (for [{:keys [name]} (or sections [])]
          [:li [:a {:href (anchor name)} name]])]]
      ;; Main Content
      [:main.stack {:role :main}
       (for [{:keys [name content html-content]} (or sections [])]
         [:div
          (if html-content
            [:<>
             (section-heading :h2 name)
             [:div {:dangerouslySetInnerHTML {:__html html-content}}]]
            [:<> (or content "")])
          [:div [:a {:href "#"} "️↑ Top"]]])]]]]])