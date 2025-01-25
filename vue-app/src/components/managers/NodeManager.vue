<template>
    <div class="node-manager">
        <!-- Render Nodes -->
        <WorkflowNode v-for="node in nodes" :key="node.id" :node="node" v-bind="node"
            v-model:localOutputData="node.outputData" @start-drag="onStartDrag" @start-link="onStartLink" />
    </div>
</template>

<script>
import { Node } from '@/models/Node';
import WorkflowNode from "@/components/WorkflowNode.vue";

export default {
    components: {
        WorkflowNode,
    },

    props: {
        nodes: {
            type: Array,
            required: true,
            validator: (value) => value.every(node => node instanceof Node)
        },
    },

    methods: {
        onStartDrag(nodeId, event) {
            this.$emit("start-drag", nodeId, event);
        },

        onStartLink(nodeId, type, position) {
            this.$emit("start-link", nodeId, type, position);
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