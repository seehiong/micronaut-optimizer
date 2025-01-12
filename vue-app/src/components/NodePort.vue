<template>
    <div :class="['base-point', type]" :style="pointStyle" :data-point-id="id" @mousedown.stop="onMouseDown">
        <!-- Port Label with Tooltip -->
        <span class="port-label" :title="label">{{ truncatedLabel }}</span>
    </div>
</template>

<script>
export default {
    name: "BasePoint",
    props: {
        id: {
            type: String,
            required: true,
        },
        type: {
            type: String,
            required: true,
            validator: (value) => ["input", "output"].includes(value),
        },
        nodeWidth: {
            type: Number,
            required: true,
        },
        offsetY: {
            type: Number,
            required: true,
        },
        label: {
            type: String,
            required: true,
        },
    },
    computed: {
        pointStyle() {
            const xPosition = this.type === "input" ? -5 : this.nodeWidth - 5;
            return {
                left: `${xPosition}px`,
                top: `${this.offsetY}px`,
            };
        },
        truncatedLabel() {
            const maxWidth = this.nodeWidth * 0.8; // 80% of node width
            const fontSize = 10; // Font size of the label
            const maxChars = Math.floor(maxWidth / fontSize); // Approximate max characters
            return this.label.length > maxChars
                ? this.label.slice(0, maxChars) + "..."
                : this.label;
        },
    },
    methods: {
        onMouseDown() {
            const position = {
                x: this.type === "input" ? 0 : this.nodeWidth,
                y: this.offsetY,
            };
            this.$emit("start-link", this.id, this.type, position);
        },
    },
};
</script>

<style scoped>
.base-point {
    position: absolute;
    width: 10px;
    height: 10px;
    background-color: #42b983;
    border-radius: 50%;
    cursor: pointer;
    z-index: 2;
}

.port-label {
    position: absolute;
    font-size: 10px;
    color: white;
    white-space: nowrap;
}

.base-point.input .port-label {
    left: 15px;
    /* Position label to the right of the input point */
    top: 50%;
    transform: translateY(-50%);
}

.base-point.output .port-label {
    right: 15px;
    /* Position label to the left of the output point */
    top: 50%;
    transform: translateY(-50%);
}
</style>