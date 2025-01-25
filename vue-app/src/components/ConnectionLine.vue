<template>
    <path :d="curvePath" :stroke="strokeColor" :stroke-width="strokeWidth" fill="none"
        :stroke-dasharray="isDotted ? '3' : undefined" />
</template>

<script>
export default {
    name: "ConnectionLine",
    props: {
        sourcePointId: { type: String, required: true },
        targetPointId: { type: String, default: null },
        mousePosition: { type: Object, default: null },
        strokeColor: { type: String, default: "#42b983" },
        strokeWidth: { type: Number, default: 1 },
        isDotted: { type: Boolean, default: false },
    },
    computed: {
        sourcePosition() {
            return this.getPosition(this.sourcePointId);
        },
        targetPosition() {
            if (this.mousePosition) {
                return {
                    x: this.mousePosition.x,
                    y: this.mousePosition.y,
                };
            }
            return this.getPosition(this.targetPointId);
        },
        curvePath() {
            let { x: x1, y: y1 } = this.sourcePosition;
            let { x: x2, y: y2 } = this.targetPosition;

            // Calculate the distance between the source and target points
            const dx = x2 - x1;
            const dy = y2 - y1;
            const distance = Math.sqrt(dx * dx + dy * dy);

            // Define thresholds for switching between straight lines and curves
            const distanceThreshold = 50; // Use straight lines if nodes are within 10 pixels
            const alignmentThreshold = 10; // Use straight lines if nodes are aligned within 5 pixels

            // Adjust source and target positions for perpendicularity
            if (Math.abs(dx) < alignmentThreshold) {
                // Nodes are vertically aligned: adjust x positions to be the same
                x1 = x2 = (x1 + x2) / 2;
            } else if (Math.abs(dy) < alignmentThreshold) {
                // Nodes are horizontally aligned: adjust y positions to be the same
                y1 = y2 = (y1 + y2) / 2;
            }

            // Ensure the line stays within the 5-pixel bounding circles
            const radius = 5; // Radius of the bounding circle
            if (distance > 0) {
                const directionX = dx / distance; // Normalized direction vector (x)
                const directionY = dy / distance; // Normalized direction vector (y)

                // Adjust source position to stay within its bounding circle
                x1 = x1 + directionX * radius;
                y1 = y1 + directionY * radius;

                // Adjust target position to stay within its bounding circle
                x2 = x2 - directionX * radius;
                y2 = y2 - directionY * radius;
            }

            // Check if nodes are close or aligned
            if (
                distance < distanceThreshold || // Nodes are close
                Math.abs(dx) < alignmentThreshold || // Nodes are vertically aligned
                Math.abs(dy) < alignmentThreshold // Nodes are horizontally aligned
            ) {
                // Use a straight line
                return `M ${x1} ${y1} L ${x2} ${y2}`;
            } else {
                // Use a quadratic Bézier curve
                const cpX = (x1 + x2) / 2; // Midpoint between x1 and x2
                const cpY = y1 - 30; // Adjust this value to control the curve's height
                return `M ${x1} ${y1} Q ${cpX} ${cpY}, ${x2} ${y2}`;
            }
        },
    },
    mounted() {
        window.addEventListener('resize', this.handleViewportChange);
        window.addEventListener('scroll', this.handleViewportChange);
    },
    beforeUnmount() {
        window.removeEventListener('resize', this.handleViewportChange);
        window.removeEventListener('scroll', this.handleViewportChange);
    },
    methods: {
        handleViewportChange() {
            // Trigger a re-render of the lines
            this.$forceUpdate();
        },

        getPosition(pointId) {
            const element = document.querySelector(`[data-point-id="${pointId}"]`);
            const svgContainer = document.querySelector('.permanent-lines'); // Reference to the SVG container
            const elementRect = element.getBoundingClientRect();
            const svgRect = svgContainer.getBoundingClientRect();

            // Calculate positions relative to the SVG container
            const x = elementRect.left - svgRect.left + elementRect.width / 2;
            const y = elementRect.top - svgRect.top + elementRect.height / 2;

            return { x, y };
        },
    },
};
</script>