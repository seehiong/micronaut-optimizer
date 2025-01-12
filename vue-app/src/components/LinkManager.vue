<template>
  <div>
    <!-- Connection Validator -->
    <ConnectionValidator ref="connectionValidator" :nodes="nodes" :lines="lines" />

    <!-- Draw permanent lines -->
    <svg class="permanent-lines">
      <ConnectionLine v-for="(line, index) in lines" :key="`${index}-${redrawTrigger}`" :sourcePointId="line.sourceId"
        :targetPointId="line.targetId" :leftPanelWidth="leftPanelWidth" />
    </svg>

    <!-- Draw the temporary dotted line -->
    <svg v-if="linking.isDrawing" class="linking-line">
      <ConnectionLine :sourcePointId="linking.sourceId" :mousePosition="mousePosition" :isDotted="true"
        :leftPanelWidth="leftPanelWidth" />
    </svg>
  </div>
</template>

<script>
import ConnectionValidator from "./ConnectionValidator.vue";
import ConnectionLine from "./ConnectionLine.vue";

export default {
  components: {
    ConnectionValidator,
    ConnectionLine,
  },
  emits: ["update-linking", "add-line"],
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
    redrawTrigger: {
      type: Number,
      required: true,
    },
  },
  mounted() {
    window.addEventListener("keydown", this.onKeydown);
  },
  beforeUnmount() {
    window.removeEventListener("keydown", this.onKeydown);
  },
  methods: {
    onKeydown(event) {
      if (event.key === "Escape") {
        this.resetLinkingState();
      }
    },

    onStartLink(nodeId, type) {
      if (!this.linking.isDrawing) {
        this.startLink(nodeId, type);
      } else {
        this.endLink(nodeId, type);
      }
    },

    startLink(nodeId, type) {
      // Determine if the port is an input or output port
      const isInputPort = type === "input";

      // For input ports, check if they already have a line
      if (isInputPort && this.$refs.connectionValidator.doesPortHaveLine(nodeId, type)) {
        console.warn(`This ${type} port already has a line.`);
        return;
      }

      // Start the linking process
      this.$emit("update-linking", {
        sourceId: nodeId,
        sourceType: type,
        targetId: null,
        targetType: null,
        isDrawing: true,
      });
    },

    endLink(nodeId, type) {
      // Ensure the source and target ports are different
      if (this.linking.sourceId === nodeId) {
        console.warn("Cannot connect a port to itself.");
        this.resetLinkingState();
        return;
      }

      // Ensure the source is an output port and the target is an input port
      if (!(this.linking.sourceType === "output" && type === "input")) {
        console.warn("Invalid connection: Source must be an output port and target must be an input port.");
        this.resetLinkingState();
        return;
      }

      // Check if the input port is already connected to another output port
      if (this.$refs.connectionValidator.isInputPortConnected(nodeId)) {
        console.warn("Input port is already connected to another output port.");
        this.resetLinkingState();
        return;
      }

      // Check if the connection is valid
      if (!this.$refs.connectionValidator.isValidConnection(this.linking.sourceId, nodeId)) {
        console.warn("Type mismatch: Cannot connect these nodes.");
        this.resetLinkingState();
        return;
      }

      // Check if a line already exists between these ports
      if (this.$refs.connectionValidator.doesLineExist(this.linking.sourceId, nodeId)) {
        console.warn("A line already exists between these ports.");
        this.resetLinkingState();
        return;
      }

      // Create the new line
      this.createLine(this.linking.sourceId, nodeId, type);
    },

    createLine(sourcePointId, targetPointId) {
      if (!this.$refs.connectionValidator.isValidConnection(sourcePointId, targetPointId)) {
        console.warn("Invalid connection.");
        return;
      }

      if (this.$refs.connectionValidator.doesLineExist(sourcePointId, targetPointId)) {
        console.warn("A line already exists between these ports.");
        return;
      }

      this.$emit("add-line", {
        sourceId: sourcePointId,
        targetId: targetPointId,
        sourceType: this.linking.sourceType,
      });
      this.resetLinkingState();
    },

    resetLinkingState() {
      this.$emit("update-linking", {
        sourceId: null,
        sourceType: null,
        targetId: null,
        isDrawing: false,
      });
    },
  },
};
</script>

<style scoped>
.permanent-lines,
.linking-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1000;
}
</style>