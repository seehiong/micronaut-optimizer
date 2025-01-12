<template>
    <line :x1="sourcePosition.x" :y1="sourcePosition.y" :x2="targetPosition.x" :y2="targetPosition.y"
        :stroke="strokeColor" :stroke-width="strokeWidth" :stroke-dasharray="isDotted ? '5' : undefined" />
</template>

<script>
export default {
    name: "ConnectionLine",
    props: {
        sourcePointId: {
            type: String,
            required: true,
        },
        targetPointId: {
            type: String,
            default: null,
        },
        mousePosition: {
            type: Object,
            default: null,
        },
        strokeColor: {
            type: String,
            default: "#42b983",
        },
        strokeWidth: {
            type: Number,
            default: 2,
        },
        isDotted: {
            type: Boolean,
            default: false,
        },
        leftPanelWidth: {
            type: Number,
            default: 0,
        },
    },
    computed: {
        sourcePosition() {
            return this.getPosition(this.sourcePointId);
        },
        targetPosition() {
            // If mousePosition is provided (during linking), use it as the target
            if (this.mousePosition) {
                return {
                    x: this.mousePosition.x - this.leftPanelWidth -5,
                    y: this.mousePosition.y +5,
                };
            }
            // Otherwise, calculate the position of the targetPointId
            return this.getPosition(this.targetPointId);
        },
    },
    methods: {
        getPosition(pointId) {
            const element = document.querySelector(`[data-point-id="${pointId}"]`);
            if (!element) {
                console.error(`Element with pointId ${pointId} not found.`);
                return { x: 0, y: 0 };
            }
            const rect = element.getBoundingClientRect();
            const x = rect.left - rect.width / 2 - this.leftPanelWidth + 2;
            const y = rect.top + rect.height + 2;
            return { x, y };
        },

    },
};
</script>

<style scoped>
/* No additional styles needed for now */
</style>