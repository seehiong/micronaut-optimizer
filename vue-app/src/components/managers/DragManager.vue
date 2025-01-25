<template>
    <template v-if="false"></template> <!-- Empty root template -->
</template>

<script>
export default {
    props: {
        nodes: Array,
        lines: Array,
        linking: Object,
    },
    data() {
        return {
            draggingNodeId: null,
            offset: { x: 0, y: 0 },
        };
    },
    methods: {
        onStartDrag(nodeId, event) {
            this.draggingNodeId = nodeId;
            const node = this.nodes.find((n) => n.id === nodeId);

            // Get the container's position
            const container = this.$el.parentElement;
            const containerRect = container.getBoundingClientRect();

            // Calculate offset relative to the container
            this.offset.x = event.clientX - containerRect.left - node.x;
            this.offset.y = event.clientY - containerRect.top - node.y;

            document.addEventListener("mousemove", this.onMouseMove);
            document.addEventListener("mouseup", this.onStopDrag);
        },
        onStopDrag() {
            this.draggingNodeId = null;
            document.removeEventListener("mousemove", this.onMouseMove);
            document.removeEventListener("mouseup", this.onStopDrag);
        },
        onMouseMove(event) {
            if (this.draggingNodeId !== null) {
                const node = this.nodes.find(n => n.id === this.draggingNodeId);

                // Get the container's position
                const container = this.$el.parentElement;
                const containerRect = container.getBoundingClientRect();

                // Update node position relative to the container
                node.x = event.clientX - containerRect.left - this.offset.x;
                node.y = event.clientY - containerRect.top - this.offset.y;

                // Update all lines connected to this node
                this.lines.forEach(line => {
                    const sourceNodeId = line.sourceId.split("-")[0];
                    const targetNodeId = line.targetId.split("-")[0];

                    if (this.draggingNodeId === sourceNodeId || this.draggingNodeId === targetNodeId) {
                        this.$emit("redraw-line");
                    }
                });
            }

            if (this.linking.isDrawing) {
                this.mousePosition = { x: event.clientX, y: event.clientY };
            }
        },
    },
};
</script>