<template>
  <div>
    <!-- Draw permanent lines -->
    <svg class="permanent-lines">
      <ConnectionLine v-for="(line, index) in lines" :key="`${index}-${redrawTrigger}`" :sourcePointId="line.sourceId"
        :targetPointId="line.targetId" />
    </svg>

    <!-- Draw the temporary dotted line -->
    <svg v-if="linking.isDrawing" class="linking-line">
      <ConnectionLine :sourcePointId="linking.sourceId" :mousePosition="mousePosition" :isDotted="true" />
    </svg>
  </div>
</template>

<script>
import { useToast } from 'vue-toastification';
import {
  isInputPortConnected,
  isLineExist,
  isValidConnection,
} from '@/utils/lineConnectionUtils';
import ConnectionLine from "@/components/ConnectionLine.vue";

const toast = useToast();

export default {
  components: {
    ConnectionLine,
  },
  emits: ["update-linking", "add-line"],
  props: {
    lines: { type: Array, required: true },
    linking: { type: Object, required: true },
    mousePosition: { type: Object, required: true },
    redrawTrigger: { type: Number, required: true },
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
      if (isInputPort && isInputPortConnected(nodeId)) {
        toast.warning(`This ${type} port already has a line.`);
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
        toast.warning("Cannot connect a port to itself.");
        this.resetLinkingState();
        return;
      }

      // Ensure the source is an output port and the target is an input port
      if (!(this.linking.sourceType === "output" && type === "input")) {
        toast.warning("Invalid connection: Source must be an output port and target must be an input port.");
        this.resetLinkingState();
        return;
      }

      // Check if the input port is already connected to another output port
      if (isInputPortConnected(nodeId)) {
        toast.warning("Input port is already connected to another output port.");
        this.resetLinkingState();
        return;
      }

      // Check if the connection is valid
      if (!isValidConnection(this.linking.sourceId, nodeId)) {
        toast.error("Type mismatch: Cannot connect these nodes.");
        this.resetLinkingState();
        return;
      }

      // Check if a line already exists between these ports
      if (isLineExist(this.linking.sourceId, nodeId)) {
        toast.warning("A line already exists between these ports.");
        this.resetLinkingState();
        return;
      }

      // Create the new line
      this.createLine(this.linking.sourceId, nodeId, type);
    },

    createLine(sourcePointId, targetPointId) {
      this.$emit("add-line", {
        sourceId: sourcePointId,
        targetId: targetPointId,
        sourceType: this.linking.sourceType,
      });
      this.resetLinkingState();
      toast.success("Connection created successfully!");
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
  z-index: 10;
}
</style>