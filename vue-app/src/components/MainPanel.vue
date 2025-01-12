<template>
  <div class="main-panel" @dragover.prevent @mousemove="onMouseMove">
    <!-- Render nodes using NodeManager -->
    <NodeManager :nodes="nodes" :lines="lines" @start-drag="onStartDrag" @start-link="onStartLink" />

    <!-- Render lines using LinkManager -->
    <LinkManager ref="linkManager" :nodes="nodes" :lines="lines" :linking="linking" :mousePosition="mousePosition"
      :leftPanelWidth="leftPanelWidth" :redrawTrigger="redrawTrigger" @start-link="onStartLink" @add-line="onAddLine"
      @update-linking="onUpdateLinking" />

    <!-- Render DragManager -->
    <DragManager ref="dragManager" :nodes="nodes" :lines="lines" :linking="linking" @redraw-line="onRedrawLine" />
  </div>
</template>

<script>
import NodeManager from "./NodeManager.vue";
import LinkManager from "./LinkManager.vue";
import DragManager from "./DragManager.vue";

export default {
  components: {
    NodeManager,
    LinkManager,
    DragManager,
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
    linking: {
      type: Object,
      required: true,
    },
    mousePosition: {
      type: Object,
      required: true,
    },
    leftPanelWidth: {
      type: Number,
      required: true,
    },
  },
  data() {
    return {
      redrawTrigger: 0, // Initialize redrawTrigger
    };
  },
  methods: {
    onMouseMove(event) {
      this.$emit("update-mouse-position", { x: event.clientX, y: event.clientY });
    },
    onStartDrag(nodeId, event) {
      this.$refs.dragManager.onStartDrag(nodeId, event);
    },
    onStartLink(nodeId, type, position) {
      this.$refs.linkManager.onStartLink(nodeId, type, position);
    },
    onAddLine(newLine) {
      this.$emit("add-line", newLine);
    },
    onUpdateLinking(linkingState) {
      this.$emit("update-linking", linkingState);
    },
    onRedrawLine() {
      // Increment the redrawTrigger to force a redraw of ConnectionLine components
      this.redrawTrigger = (this.redrawTrigger + 1) % 1000; // Reset after reaching 1000
    },
  },
};
</script>

<style scoped>
.main-panel {
  flex: 1;
  position: relative;
  background-color: #fff;
}
</style>