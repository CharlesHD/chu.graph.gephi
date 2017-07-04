(def +version+ "0.0.2")

(set-env!
 :source-paths #{"src"}
 :target-path "tmp"
 :dependencies '[[org.clojure/clojure "1.9.0-alpha17"]
                 [adzerk/bootlaces "0.1.13" :scope "test"]
                 [adzerk/boot-test "1.2.0" :scope "test"]
                 [chulper "1.1.1"]
                 [chu.graph "0.1.6.1"]
                 [org.gephi/gephi-toolkit "0.9.1"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/test.check "0.9.0"]])

(require '[adzerk.bootlaces :refer :all])
(require '[adzerk.boot-test :refer :all])

(task-options!
 pom {:project 'chu.graph.gephi
      :version +version+
      :description "a gephi implementation of chu.graph"
      :url "https://github.com/CharlesHD/chu.graph.gephi"
      :scm {:url "https://github.com/CharlesHD/chu.graph.gephi"}}
 jar {:manifest {"Foo" "bar"}})

(bootlaces! +version+)

(deftask testing
  "Profile setup for running tests."
  []
  (set-env! :source-paths #(conj % "test"))
  identity)
