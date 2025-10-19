package com.powergraph.inspect.service.impl;

import com.powergraph.inspect.service.GraphService;
import org.springframework.stereotype.Service;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Service
public class GraphServiceImpl implements GraphService {

    @Autowired
    private Driver driver;

    /**
     * --- 【核心修复】 ---
     * 重写整个方法，使用单一的、原子化的查询来保证数据一致性。
     */
    @Override
    public Map<String, Object> getGraphData() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        // 使用一个 Set 来跟踪已添加的节点ID，防止重复添加
        Set<Long> addedNodeIds = new HashSet<>();

        try (Session session = driver.session()) {
            // 1. 【重要】使用单一查询
            // 这个查询会返回所有的关系，以及每条关系所连接的起点(n)和终点(m)。
            // 这就保证了我们拿到的每一条边，它的起点和终点都一定是存在的。
            Result queryResult = session.run("MATCH (n)-[r]->(m) RETURN n, r, m");

            // 2. 遍历查询结果
            while (queryResult.hasNext()) {
                Record record = queryResult.next();
                Node startNode = record.get("n").asNode();
                Node endNode = record.get("m").asNode();
                Relationship relationship = record.get("r").asRelationship();

                // 3. 处理起点
                if (addedNodeIds.add(startNode.id())) { // 如果是第一次遇到这个节点
                    nodes.add(convertNodeToMap(startNode));
                }

                // 4. 处理终点
                if (addedNodeIds.add(endNode.id())) { // 如果是第一次遇到这个节点
                    nodes.add(convertNodeToMap(endNode));
                }

                // 5. 处理关系
                edges.add(convertRelationshipToMap(relationship));
            }

            // 6. 额外补充：包含无任何关系的孤立节点
            Result orphanNodes = session.run("MATCH (n) WHERE NOT (n)--() RETURN n");
            while (orphanNodes.hasNext()) {
                Record record = orphanNodes.next();
                Node node = record.get("n").asNode();
                if (addedNodeIds.add(node.id())) {
                    nodes.add(convertNodeToMap(node));
                }
            }
        }

        result.put("nodes", nodes);
        result.put("edges", edges);
        return result;
    }

    /**
     * 辅助方法：将 Neo4j 的 Node 对象转换为前端需要的 Map 格式
     */
    private Map<String, Object> convertNodeToMap(Node node) {
        Map<String, Object> nodeData = new HashMap<>(node.asMap());
        nodeData.put("id", String.valueOf(node.id())); // 确保ID是字符串

        // 添加节点标签作为类型
        Iterator<String> labelIterator = node.labels().iterator();
        if (labelIterator.hasNext()) {
            nodeData.put("type", labelIterator.next().toLowerCase());
        }
        return nodeData;
    }

    /**
     * 辅助方法：将 Neo4j 的 Relationship 对象转换为前端需要的 Map 格式
     */
    private Map<String, Object> convertRelationshipToMap(Relationship relationship) {
        Map<String, Object> edgeData = new HashMap<>(relationship.asMap());
        edgeData.put("id", String.valueOf(relationship.id()));
        edgeData.put("source", String.valueOf(relationship.startNodeId()));
        edgeData.put("target", String.valueOf(relationship.endNodeId()));
        edgeData.put("relationship", relationship.type());
        return edgeData;
    }


    // 下面的增删改查方法保持不变，它们是正确的
    @Override
    public Map<String, Object> addNode(Map<String, Object> nodeData) {
        String id = (String) nodeData.get("id");
        String type = (String) nodeData.get("type");

        if (id != null && type != null) {
            try (Session session = driver.session()) {
                // 构建Cypher查询语句
                StringBuilder query = new StringBuilder();
                query.append("CREATE (n:").append(type.substring(0, 1).toUpperCase() + type.substring(1)).append(" {");

                boolean first = true;
                for (Map.Entry<String, Object> entry : nodeData.entrySet()) {
                    if (!first) query.append(", ");
                    query.append(entry.getKey()).append(": $").append(entry.getKey());
                    first = false;
                }
                query.append("}) RETURN n");

                Result result = session.run(query.toString(), nodeData);
                if (result.hasNext()) {
                    Record record = result.next();
                    Node created = record.get("n").asNode();
                    return convertNodeToMap(created);
                }
            }
        }
        return java.util.Collections.emptyMap();
    }

    @Override
    public void updateNode(String nodeId, Map<String, Object> nodeData) {
        try (Session session = driver.session()) {
            // 构建SET子句
            StringBuilder setClause = new StringBuilder();
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("nodeId", Long.parseLong(nodeId));

            boolean first = true;
            for (Map.Entry<String, Object> entry : nodeData.entrySet()) {
                if (!"id".equals(entry.getKey()) && !"type".equals(entry.getKey())) {
                    if (!first) setClause.append(", ");
                    setClause.append("n.").append(entry.getKey()).append(" = $").append(entry.getKey());
                    parameters.put(entry.getKey(), entry.getValue());
                    first = false;
                }
            }

            String query = "MATCH (n) WHERE id(n) = $nodeId SET " + setClause.toString();
            session.run(query, parameters);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid node ID format", e);
        }
    }

    @Override
    public void deleteNode(String nodeId) {
        try (Session session = driver.session()) {
            String query = "MATCH (n) WHERE id(n) = $nodeId DETACH DELETE n";
            session.run(query, java.util.Collections.singletonMap("nodeId", Long.parseLong(nodeId)));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid node ID format", e);
        }
    }

    @Override
    public void addEdge(Map<String, Object> edgeData) {
        String sourceId = (String) edgeData.get("source");
        String targetId = (String) edgeData.get("target");
        String relationship = (String) edgeData.get("relationship");

        if (sourceId != null && targetId != null && relationship != null) {
            try (Session session = driver.session()) {
                StringBuilder query = new StringBuilder();
                query.append("MATCH (a), (b) WHERE id(a) = $sourceId AND id(b) = $targetId ");
                query.append("CREATE (a)-[r:").append(relationship).append(" {");

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("sourceId", Long.parseLong(sourceId));
                parameters.put("targetId", Long.parseLong(targetId));

                boolean first = true;
                for (Map.Entry<String, Object> entry : edgeData.entrySet()) {
                    // 跳过source, target, relationship字段
                    if (!"source".equals(entry.getKey()) && !"target".equals(entry.getKey()) && !"relationship".equals(entry.getKey())) {
                        if (!first) query.append(", ");
                        query.append(entry.getKey()).append(": $").append(entry.getKey()).append("Param");
                        parameters.put(entry.getKey() + "Param", entry.getValue());
                        first = false;
                    }
                }
                query.append("}]->(b) RETURN r");

                session.run(query.toString(), parameters);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid node ID format", e);
            }
        }
    }

    @Override
    public void updateEdge(String edgeId, Map<String, Object> edgeData) {
        try (Session session = driver.session()) {
            // 构建SET子句
            StringBuilder setClause = new StringBuilder();
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("edgeId", Long.parseLong(edgeId));

            boolean first = true;
            for (Map.Entry<String, Object> entry : edgeData.entrySet()) {
                // 跳过source, target, relationship字段
                if (!"source".equals(entry.getKey()) && !"target".equals(entry.getKey()) && !"relationship".equals(entry.getKey())) {
                    if (!first) setClause.append(", ");
                    setClause.append("r.").append(entry.getKey()).append(" = $").append(entry.getKey());
                    parameters.put(entry.getKey(), entry.getValue());
                    first = false;
                }
            }

            String query = "MATCH ()-[r]->() WHERE id(r) = $edgeId SET " + setClause.toString();
            session.run(query, parameters);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid edge ID format", e);
        }
    }

    @Override
    public void deleteEdge(String edgeId) {
        try (Session session = driver.session()) {
            String query = "MATCH ()-[r]->() WHERE id(r) = $edgeId DELETE r";
            session.run(query, java.util.Collections.singletonMap("edgeId", Long.parseLong(edgeId)));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid node ID format", e);
        }
    }
}