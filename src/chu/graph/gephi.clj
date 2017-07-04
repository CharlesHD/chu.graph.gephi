(ns chu.graph.gephi
  (:require [chu.graph :as g]
            [chu.graph.id-wrapper :refer [wrap]]
            [chulper.core :as chulper])
  (:import org.gephi.graph.api.GraphController
           org.gephi.project.api.ProjectController
           org.gephi.statistics.plugin.GraphDistance
           org.openide.util.Lookup))

(defrecord GephiGraph [fct g edge-weight])

(defn make-gephi-graph
  [weight-key]
  (let [lkp (Lookup/getDefault)
        pc (.lookup  lkp ProjectController)
        workspace (do (.newProject pc)
                      (.getCurrentWorkspace pc))
        graph-model (.getGraphModel (.lookup lkp GraphController))
        node-table (.getNodeTable graph-model)
        edge-table (.getEdgeTable graph-model)
        factory (.factory graph-model)
        graph (.getDirectedGraph graph-model)]
    (.addColumn node-table "object" clojure.lang.PersistentArrayMap)
    (.addColumn edge-table "object" clojure.lang.PersistentArrayMap)
    (->GephiGraph factory graph weight-key)))

(defn- mk-node
  [n]
  {"node" n})
(defn- get-node
  [g node]
  (.getNode (:g g) (str node)))

(defn- get-edge
  [g {:keys [from to]}]
  (.getEdge (:g g) (get-node g from) (get-node g to)))

(defn- add-node
  [{:keys [fct g] :as r} node]
  (when-not (get-node r node)
    (let [n (.newNode fct (str node))]
      (.setAttribute n "object" (mk-node node))
      (.addNode g n)))
  r)

(defn- add-link
  [{:keys [fct g weight-key] :as r}
   {:keys [from to params] :as l}]
  (when-not (get-edge r l)
    (add-node r from)
    (add-node r to)
    (let [l2 (.newEdge
              fct
              (.getNode g (str from))
              (.getNode g (str to))
              0
              (get params weight-key 1.0)
              true)]
      (.addEdge g l2)))
  r)

(defn- graph->gephi
  [g wk]
  (let [empty (make-gephi-graph wk)]
    (g/reduce-graph
     add-node
     add-link
     empty
     g)))

(defn centralities
  [g wk]
  (let [wg (wrap g)
        gg (graph->gephi (:g wg) wk)
        gd (GraphDistance.)
        mp (.createIndiciesMap gd (:g gg))
        ind->node (->> mp
                       (into {})
                       (chulper/map-keys (comp #(get % "node") #(.getAttribute % "object")))
                       (chulper/map-inverse)
                       (chulper/map-vals first)
                       (chulper/map-vals (:backward (:bij wg)))
                       (into (sorted-map)))
        res (->> (.calculateDistanceMetrics gd (:g gg) mp true true)
                 (into {})
                 (chulper/map-vals #(zipmap (vals ind->node) (into [] %))))]
    res))
