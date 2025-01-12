<template>
    <div>
        <!-- Save and Load Buttons -->
        <button @click="saveProblem">Save Problem</button>
        <button @click="loadProblem">Load Problem</button>

        <!-- Optimization Trigger -->
        <button v-if="hasOptimizationTrigger" @click="optimizeProblem">
            Optimize Problem
        </button>
    </div>
</template>

<script>
export default {
    name: "LocalPersistent",
    props: {
        nodes: {
            type: Array,
            required: true,
        },
        lines: {
            type: Array,
            required: true,
        },
        hasOptimizationTrigger: {
            type: Boolean,
            default: false,
        },
    },
    methods: {
        // Save the current problem state to local storage
        saveProblem() {
            const state = {
                nodes: this.nodes,
                lines: this.lines,
            };
            localStorage.setItem("savedProblem", JSON.stringify(state));
            console.log("Problem saved to local storage!");
        },

        // Load the problem state from local storage
        loadProblem() {
            const savedState = localStorage.getItem("savedProblem");
            if (savedState) {
                const state = JSON.parse(savedState);
                this.$emit("load-state", state);
                console.log("Problem loaded from local storage!");
            } else {
                console.warn("No saved problem found in local storage.");
            }
        },

        // Simulate optimization and save the problem
        optimizeProblem() {
            console.log("Optimizing problem...");
            // Simulate optimization logic here (e.g., modify nodes or lines)
            const optimizedNodes = this.nodes.map((node) => ({
                ...node,
                name: `Optimized ${node.name}`, // Example: Modify node names
            }));

            // Emit the optimized state
            this.$emit("optimize-state", optimizedNodes);

            // Save the optimized problem
            this.saveProblem();
        },
    },
};
</script>

<style scoped>
button {
    margin: 5px;
    padding: 10px;
    background-color: #42b983;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
}

button:hover {
    background-color: #3aa876;
}
</style>