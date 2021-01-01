Lei comes with its own documentation generation system. This website was generated entirely from code annotations and Markdown files. The same API that Lei uses to document itself is available to you for any patterns you define.

### Overview

Generating a full documentation site is as simple as defining a function and telling Lei to call it.

Here is a simple example with an Intro section, two custom patterns (`core/pattern` and `core/other-pattern`), and a Conclusion:

```clj
(ns my.design-system.docs
  (:require
   [garden.core :as garden]
   [lei.docs :as docs]
   [my.design-system.core :as core]
   [rum.core :as rum]))
 
(def my-docs-styles
  [[:* {:color :red}]])

(defn index-html []
  (str
   "<!doctype html>\n"
   (rum/render-static-markup
    (ld/page {:title "My Design System"
              :description "My very own design system!!1"
              :head-html
              [:<>
               (ld/inline-style (garden/css my-docs-styles))
               ...additional scripts and stuff go here...]
              :sections
              [{:name "Intro"
                :html-content (docs/path->html "md/intro.md")}
               (ld/var->map #'core/pattern)
               (ld/var->map #'core/other-pattern)
               {:name "Conclusion"
                :html-content (docs/path->html "md/conclusion.md")}]}))))
```

Now, you can use Lei's built-in CLI to write the output of this function to a file:

```bash
clj -m lei.gen my.design-system.docs/index-html
```

This will output your HTML to `dist/index.html`. To change the path, specify a `:path` option:

```bash
clj -m lei.gen my.design-system.docs/index-html :path path/to/file.html
```

#### Gotchas

##### Creating output directories

Lei does not generate output directories for you. So the above default command will fail if `dist` does not already exist.

##### BYOR - Bring Your Own Renderer!

Lei uses [Rum](https://github.com/tonsky/rum) internally to render its own documentation, but it does not declare Rum as a production dependency. This gives you greater flexibility to choose your own HTML renderer, but it does mean you have to declare it explicitly.

### Annotating Patterns

Consider this docs page:

```clj
(docs/page {:title "My Design System"
            :sections
            [(docs/var->map #'core/pattern)
             (docs/var->map #'core/other-pattern)]}))))
```

The patterns in this hypothetical design system are passed to Lei's `docs/var->map` as [Vars](https://clojure.org/reference/vars) (hence the `#'` [reader macro](https://clojure.org/reference/special_forms#var)). This allows Lei to access the [metadata](https://clojure.org/reference/metadata) defined for your functions, in this case `core/pattern` and `core/other-pattern`. Here is what those functions might look like, complete with metadata:

```clj
(ns my.design-system.core)

(defn
  ^{:lei/name "My Pattern"
    :lei/description "The best visual design pattern in the world."
    :lei/options
    [{:name :option-one
      :description "Do a thing."
      :default "nil"}
     {:name :option-two
      :description "Do something else."
      :default {:this :can
                :be :whatever}}]
    :lei/examples
    [{:name "Passing an option"
      :form `(pattern {:option-one :some-value}
      :description "This does a really cool thing with `:some-value`")}
     {:name "Composing options"
      :form `(pattern {:option-one :some-value
                       :option-two :something})
      :description "This example uses BOTH options. Mind = blown."}]}
  pattern
  [{:keys [option-one option-two]}]
  ,,,)

(defn
  ^{:lei/name "My Other Pattern"
    :lei/description "The best visual design pattern in the world."
    ;; options and examples are not required.
    }
  other-pattern
  []
  ,,,)
```

Given these definitions, the `docs/var->map` calls above generate a top-level section for each of the two patterns. Each option and each example gets its own subsection automatically. If no options and/or examples are given, those sections are simply omitted.

### Doc components

In addition to the `lei.docs/page` function (see the example above), Lei comes with several built-in components for rendering pieces of documentation in a modular way. They are defined as [multimethods](https://clojure.org/reference/multimethods) that dispatch off of the `:lei/renderer` value in the maps that define your patterns and their options and examples. This affords fine-grained control over how every aspect of your patterns are documented without much boilerplate.

Lei defines the following as multimethods:

* `lei.docs/pattern` renders docs for a top-level design pattern.
* `lei.docs/example` renders docs for a single pattern example.
* `lei.docs/clj-snippet` renders the formatted, *unevaluated* example snippet specified as `:form`.
* `lei.docs/garden-result` renders a Garden code snippet, i.e. the evaluated `form` as Clojure code, for a single example.
* `lei.docs/css-result` renders the final CSS code snippet for a single example. The default renderer evaluates the `form` and then runs it through `garden.core/css`.
* `lei.docs/option` renders docs for a single pattern option.

Consider this pattern:

```clj
(ns my.design-system.core)

(defn
  ^{:lei/name "A Beautiful Unicorn!"
    :lei/renderer :unicorn
    :lei/description "This pattern's dispatch value is `:unicorn`."
    :lei/options
    [{:name :sunshine
      :lei/renderer :special-option
      :description "This option's dispatch value is `:special-option`."}
     {:name :normal-option
      :description "This will use the default option renderer."}]
    :lei/examples
    [{:name "Daisies"
      :lei/renderer :special-example
      :form `(unicorn {,,,})
      :description "This example's dispatch value is `:special-example`.")}
     {:name "Normal Example"
      :form `(unicorn {,,,})
      :description "This will use the default example renderer."}]}
  unicorn
  [opts]
  ,,,)
```

This specifies a custom renderer for one of its options, one of its examples, and for the pattern itself.

Now, in your top-level docs code, write a normal `defmethod` for each special dispatch value you referenced above:

```clj
(ns my.design-system.docs
  (:require
   [garden.core :as garden]
   [lei.docs :as ld]))

(defmethod docs/pattern :unicorn
  [{:lei/keys [name description options examples]
    ;; destructure these if you want to use
    ;; Clojure's default metadata:
    :keys [doc file line]}]
  [:<>
   (docs/section-heading :h2 name)
   [:p "🦄🌈✨ PRETTY UNICORN PATTERN! " description]
   ;; loop over examples/options...
   ])

(defmethod docs/option :special-option
  [{:keys [name description default]}]
  [:div.special-option ,,,])

(defmethod docs/example :special-example
  [{:keys [name description form]}]
  [:div.special-example ,,,])

(defmethod docs/garden-result :special-example
  [{:keys [name description form]}]
  [:pre [:code.hljs.clj
         ";; 🦄🌈✨ pretty Unicorn Garden code!\n"
         (eval form)]])

(defmethod docs/css-result :special-example
  [{:keys [name description form]}]
  [:pre [:code.hljs.css
         "/* 🦄🌈✨ pretty Unicorn CSS */"
         (garden)]])
```

Note that `garden-result` and `css-result` are passed the full example map as defined in your pattern's metadata.