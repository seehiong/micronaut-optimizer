<template>
    <div class="node-manager">
        <!-- Render Nodes -->
        <BaseNode v-for="node in nodes" :key="node.id" :id="node.id" :name="node.name" :x="node.x" :y="node.y"
            :inputTypes="node.inputTypes" :outputTypes="node.outputTypes" :textInput="node.textInput"
            :textOutput="node.textOutput" :triggerAction="node.triggerAction" :nodeData="node.nodeData"
            @start-drag="onStartDrag" @start-link="onStartLink" @trigger="onTrigger" @update-node-data="onUpdateNodeData" />

        <!-- TransformManager -->
        <TransformManager ref="transformManager" :nodes="nodes" :lines="lines" />
    </div>
</template>

<script>
import BaseNode from "./WorkflowNode.vue";
import TransformManager from "./TransformManager.vue";

export default {
    components: {
        BaseNode,
        TransformManager
    },
    props: {
        nodes: {
            type: Array,
            required: true,
        },
        lines: {
            type: Array,
            required: true,
        },
    },
    methods: {
        onStartDrag(nodeId, event) {
            this.$emit("start-drag", nodeId, event);
        },
        onStartLink(nodeId, type, position, portIndex) {
            this.$emit("start-link", nodeId, type, position, portIndex);
        },
        onTrigger(payload) {
            this.$refs.transformManager.onTrigger(payload, this.nodes, this.lines);
        },
        onUpdateNodeData(nodeId, newNodeData) {
            // Find the node and update its nodeData
            const node = this.nodes.find((node) => node.id === nodeId);
            if (node) {
                node.nodeData = newNodeData;
            }
        },
    },
};
</script>

<style scoped>
.node-manager {
    position: relative;
    width: 100%;
    height: 100%;
}
</style>