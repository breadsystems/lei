(ns lei.docs
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [garden.core :as garden]
   [markdown.core :as md]))

  (defn path->html [path]
    (-> (io/resource path)
        slurp
        md/md-to-html-string))

(defn slug [& ss]
  (if ss
    (str/lower-case
     (str/join "-" (map #(str/replace % #" " "-") (filter string? ss))))
    ""))

(defn anchor [& ss]
  (if ss
    (str "#" (apply slug ss))
    ""))

(defn section-heading [tag & sections]
  (if (seq sections)
    [:<>
     [:a {:name (apply slug sections)}]
     [:a {:href (apply anchor sections)} [tag (last sections)]]]
    ""))

(defn docs [data]
  (let [{:lei/keys [name description options examples]
         :keys [doc]}
        data
        section-name name]
    [:article
     (section-heading :h2 section-name)
     [:p (str (or description doc))]
     (when examples
       [:section
        (section-heading :h3 name "Examples")
        [:<> (for [{:keys [name form desc]} examples]
               [:section
                (section-heading :h4 section-name "examples" name)
                [:p desc]
              ;; TODO code formatting
                [:pre (str form)]
                [:p "Result:"]
                [:pre (str (eval form))]])]])
     (when options
       [:section
        (section-heading :h3 name "Options")
        [:<> (for [{:keys [name desc default]} options]
               [:section
                (section-heading :h4 section-name "options" name)
                [:p desc " "
                 (when default [:strong "Default: " [:code default]])]])]])]))

(defn var->docs [v]
  (let [m (meta v)]
    {:name (:lei/name m)
     :content (docs m)}))

(defmulti render :lei/renderer)

(defmethod render :default [{:keys [title styles heading subheading sections]}]
  [:html
   [:head
    [:meta {:charset "utf-8"}]
    [:title title]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:style {:dangerouslySetInnerHTML {:__html (garden/css styles)}}]]
   [:body
    [:header
     [:h1 (or heading title)]
     (when subheading [:h2 subheading])]
    [:div.with-sidebar
     [:div
      [:nav
       [:ul
        (for [{:keys [name]} (or sections [])]
          [:li [:a {:href (slug name)} name]])]]
      [:div.stack
       (for [{:keys [name content html-content]} (or sections [])]
         [:div
          (if html-content
            [:<>
             (section-heading :h2 name)
             [:div {:dangerouslySetInnerHTML {:__html html-content}}]]
            [:<> (or content "")])
          [:div [:a {:href "#"} "️↑ Top"]]])]]]]])