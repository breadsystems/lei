(ns lei.docs
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [clojure.walk :as walk]
   [garden.core :as garden]
   [garden.selectors :as s]
   [garden.units :refer [em percent]]
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
    (str (list (symbol (.-unit this))
               (.-magnitude this)))))

(defn path->html [path]
  ;; TODO mark header sections
  (some-> (io/resource path) slurp md/md-to-html-string))

(defn dangerous [tag attrs & [html]]
  (let [[attrs html] (if (map? attrs )
                       [attrs html]
                       [{} attrs])]
    [tag (merge attrs {:dangerouslySetInnerHTML {:__html (or html "")}})]))

(defn inline-style [css]
  (dangerous :style css))

(defn inline-script [js]
  (dangerous :script js))

(defn markdown-section [{:keys [name path]}]
  {:name name
   :html-content (path->html path)})

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
   [:a.margin-top:0 {:href (apply anchor sections)} [tag (last sections)]]])

(def ^:dynamic *format-opts*
  {:measure 70
   :zprint-opts {;; `:color? true` prints shell control chars,
                 ;; which we don't want in the browser.
                 :color? false
                 :map {:comma? false}}})

(defn code-example-styles [{:keys [default-label-styles
                                   selected-label-styles]}]
  (let [selected-label-styles (merge {:text-decoration :underline}
                                     selected-label-styles)]
    [[:.example-result
      [:pre :nav {:margin-bottom 0
                  :margin-top 0}]
      [:nav {:display :flex
             :justify-content :space-evenly
             :border "1px solid black"}
       [:label (merge {:margin-top 0
                       :flex-basis (percent 50)
                       :padding (em 0.5)
                       :cursor :pointer}
                      default-label-styles)]
       [(s/+ :label :label) {:border-left "1px solid black"}]]
      [(s/attr :data-tab) {:margin-top 0
                           :display :none}]]
     [(s/attr :name=tab-toggle) {:display :none}]
     ;; Selected labels
     ["[name=tab-toggle][value=css]:checked ~ * [for=tab--css]"
      selected-label-styles]
     ["[name=tab-toggle][value=garden]:checked ~ * [for=tab--garden]"
      selected-label-styles]
     ;; Selected code blocks
     ["[name=tab-toggle][value=css]:checked ~ * [data-tab=css]"
      {:display :block}]
     ["[name=tab-toggle][value=garden]:checked ~ * [data-tab=garden]"
      {:display :block}]]))

;; TODO aliases in snippets

(defmulti format-clj (fn [opts _form]
                                (:formatter opts)))
(defmethod format-clj :default [format-opts form]
  (let [{:keys [measure zprint-opts]} format-opts]
    (zp/czprint-str form measure zprint-opts)))

(defmulti clj-snippet :lei/renderer)
(defmethod clj-snippet :default [{:keys [form]}]
  [:pre [:code.hljs.clj (format-clj *format-opts* form)]])

(defmulti garden-result :lei/renderer)
(defmethod garden-result :default [{:keys [form]}]
  (let [garden-form (walk/postwalk as-garden (eval form))]
    [:pre [:code.hljs.clj (format-clj *format-opts* garden-form)]]))

(defn form->css-comment [form]
  (let [newline "\n * "]
    (as-> form $
      (format-clj *format-opts* $)
      (str/split $ #"\n")
      (str/join newline $)
      (str "/*" newline $ "\n */\n"))))

(defmulti css-result :lei/renderer)
(defmethod css-result :default [{:keys [form]}]
  [:pre [:code.hljs.css
         (form->css-comment (list `garden/css form))
         (garden/css (eval form))]])

(defmulti example :lei/renderer)
(defmethod example :default [ex]
  [:div.example-result {:data-tabs 2}
   (clj-snippet ex)
   [:nav
    [:label {:data-nav-tab "css"
             :for "tab--css"} "CSS Result"]
    [:label {:data-nav-tab "garden"
             :for "tab--garden"} "Garden Result"]]
   [:div {:data-tab "garden"}
    (garden-result ex)]
   [:div {:data-tab "css"}
    (css-result ex)]])

(defmulti option :lei/renderer)
(defmethod option :default [{:keys [required? description default]}]
  [:div
   (when required? [:strong "Required. "])
   (dangerous :div (md/md-to-html-string description))
   (when default [:div " Default: " [:code (as-garden default)]])])

(defmulti pattern :lei/renderer)
(defmethod pattern :default [data]
  (let [{:lei/keys [name description options examples]
         :keys [doc file line]}
        data
        section-name name]
    [:<>
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
        (for [{:keys [name] :as opt} options]
          [:div
           (section-heading :h4 section-name "options" name)
           (option opt)])])]))

(defmethod pattern :lei/docfn [data]
  (let [{:lei/keys [name description]
         :keys [doc]}
        data
        section-name name]
    [:<>
     (section-heading :h3 section-name)
     (dangerous :p (md/md-to-html-string
                    (or description doc)))]))

(defn api-section [{:keys [name vars]}]
  {:name name
   :content
   [:<>
    (section-heading :h2 name)
    (map (comp pattern meta) vars)]})

(defmulti nav :lei/renderer)
(defmethod nav :default [{:keys [sections]}]
  [:nav {:role :navigation}
   [:ul {:role :list}
    (for [{:keys [name children]} (or sections [])]
      [:li
       [:a {:href (anchor name)} name]
       (when children
         [:ul {:role :list}
          (for [{:keys [name]} children]
            [:li
             [:a {:href (anchor name)} name]])])])]])

(defn var->map [v]
  (let [m (meta v)]
    {:name (:lei/name m)
     :content (pattern m)}))

(defn
  ^{:lei/name `page
    :lei/renderer :lei/docfn
    :lei/description "Render a full page of documentation with zero or more sections."
    :lei/options
    [{:name :title
      :description "The HTML document `title`. Also used for the default `h1` if no
                    header-html is given."}
     {:name :description
      :description "Text for the SEO meta description."}
     {:name :head-html
      :description "Arbitrary markup for the `head` element, e.g. scripts and styles."}
     {:name :header-html
      :description "Arbitrary markup for the `header` element at the top of the page.
                    If none is given, defaults to `[:h1 title]`"}]}
  page [{:keys [title
                description
                head-html
                header-html
                sections]}]
  [:html {:lang "en-US"}
   [:head
    [:meta {:charset "utf-8"}]
    [:title title]
    (when description
      [:meta {:name "description"
              :content description}])
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]
    head-html]
   [:body
    [:header
     (if header-html
       header-html
       [:h1 title])]
    [:input {:type :radio
             :name :tab-toggle
             :value "css"
             :id "tab--css"
             :checked true}]
    [:input {:type :radio
             :name :tab-toggle
             :value "garden"
             :id "tab--garden"}]
    [:div.with-sidebar
     [:div
      ;; Sidebar
      (nav {:sections sections})
      ;; Main Content
      [:main.big-stack {:role :main}
       (for [{:keys [name children content html-content]} (or sections [])]
         [:div
          [:article.stack
           (cond
             html-content [:<>
                           (section-heading :h2 name)
                           (dangerous :div html-content)]
             ;; Lei's opinion is that hierarchy in the IA is only for the
             ;; nav; it shouldn't influence how the content sections themselves
             ;; are rendered. So here, we flatten section children and splice
             ;; them into the list of top-level sections.
             children     [:<>
                           (section-heading :h2 name)
                           content
                           (map :content children)]
             content      content
             :else        [:p {:style {:color :red}}
                           "Don't know how to render section: " name])
           [:div [:a {:href "#"} "️↑ Top"]]]])]]]]])