(ns lei.docs
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [clojure.walk :as walk]
   [garden.core :as garden]
   [garden.selectors]
   [markdown.core :as md]
   [zprint.core :as zp]))

(defprotocol GardenRenderable
  (as-garden [this]))

(extend-protocol GardenRenderable
  java.lang.Object
  (as-garden [this]
    this)

  garden.selectors.CSSSelector
  (as-garden [this]
    (str (symbol (.-selector this))))

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

(defn inline-style [css]
  (dangerous :style css))

(defn inline-script [js]
  (dangerous :script js))

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

(def ^:dynamic *format-opts*
  {:measure 70
   :zprint-opts {;; `:color? true` prints shell control chars,
                 ;; which we don't want in the browser.
                 :color? false}})

;; TODO aliases in snippets

(defmulti format-clj (fn [opts _form]
                                (:formatter opts)))
(defmethod format-clj :default [format-opts form]
  (let [{:keys [measure zprint-opts]} format-opts]
    (zp/czprint-str form measure zprint-opts)))

(defmulti clj-snippet :renderer)
(defmethod clj-snippet :default [{:keys [form]}]
  [:pre [:code.lang-clojure (format-clj *format-opts* form)]])

(defmulti garden-result :renderer)
(defmethod garden-result :default [{:keys [form]}]
  (let [garden-form (walk/postwalk as-garden (eval form))]
    [:pre [:code.lang-clojure (format-clj *format-opts* garden-form)]]))

(defn form->css-comment [form]
  (let [newline "\n * "]
    (as-> form $
      (format-clj *format-opts* $)
      (str/split $ #"\n")
      (str/join newline $)
      (str "/*" newline $ "\n */\n"))))

(defmulti css-result :renderer)
(defmethod css-result :default [{:keys [form]}]
  [:pre [:code.lang-css
         (form->css-comment (list 'garden.core/css form))
         (garden/css (eval form))]])

(defmulti example :renderer)
(defmethod example :default [ex]
  [:div
   (clj-snippet ex)
   [:p "Result:"]
   [:div {:data-tabs 2}
    [:div {:data-tab "garden"}
     (garden-result ex)]
    [:div {:data-tab "css"}
     (css-result ex)]]])

(defmulti pattern :renderer)
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
        (for [{:keys [name description] :as ex} examples]
          [:div
           (section-heading :h4 section-name "examples" name)
           (dangerous :div (md/md-to-html-string description))
           (example ex)])])
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

(defmulti page :renderer)
(defmethod page :default [{:keys [title
                                  head-html
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
    (inline-style (garden/css styles))
    head-html]
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