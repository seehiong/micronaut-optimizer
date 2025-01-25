<template>
    <div>
        <!-- Save Dialog -->
        <div v-if="showSaveDialog" class="dialog-overlay">
            <div class="dialog">
                <h3>Select a Save Slot</h3>
                <div class="save-slots">
                    <button v-for="slot in 5" :key="slot" @click="onSaveToSlot(slot)"
                        :class="{ active: selectedSlot === slot }">
                        Slot {{ slot }}
                    </button>
                </div>
                <button @click="onCloseSaveDialog">Cancel</button>
            </div>
        </div>

        <!-- Load Dialog -->
        <div v-if="showLoadDialog" class="dialog-overlay">
            <div class="dialog">
                <h3>Select a Save Slot to Load</h3>
                <ul>
                    <li v-for="slot in 5" :key="slot" @click="onLoadFromSlot(slot)"
                        :class="{ active: selectedSlot === slot }">
                        Slot {{ slot }}
                    </li>
                </ul>
                <button @click="onCloseLoadDialog">Cancel</button>
            </div>
        </div>
    </div>
</template>

<script>
import { useToast } from "vue-toastification";
import pako from 'pako';

const toast = useToast();

// IndexedDB utility functions
const openDB = () => {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open('ProblemSolverDB', 1);

        request.onupgradeneeded = (event) => {
            const db = event.target.result;
            if (!db.objectStoreNames.contains('savedProblems')) {
                db.createObjectStore('savedProblems', { keyPath: 'slot' });
            }
        };

        request.onsuccess = () => resolve(request.result);
        request.onerror = () => reject(request.error);
    });
};

const saveToIndexedDB = async (slot, data) => {
    const db = await openDB();
    const transaction = db.transaction('savedProblems', 'readwrite');
    const store = transaction.objectStore('savedProblems');
    store.put({ slot, data });
};

const loadFromIndexedDB = async (slot) => {
    const db = await openDB();
    const transaction = db.transaction('savedProblems', 'readonly');
    const store = transaction.objectStore('savedProblems');
    const request = store.get(slot);
    return new Promise((resolve, reject) => {
        request.onsuccess = () => resolve(request.result?.data);
        request.onerror = () => reject(request.error);
    });
};

export default {
    name: "LocalPersistent",

    props: {
        nodes: { type: Array, required: true },
        lines: { type: Array, required: true },
    },

    data() {
        return {
            showSaveDialog: false,
            showLoadDialog: false,
            selectedSlot: null,
        };
    },

    methods: {
        openSaveDialog() {
            this.showSaveDialog = true;
            this.selectedSlot = null;
        },

        openLoadDialog() {
            this.showLoadDialog = true;
            this.selectedSlot = null;
        },

        onCloseSaveDialog() {
            this.showSaveDialog = false;
            this.selectedSlot = null;
        },

        onCloseLoadDialog() {
            this.showLoadDialog = false;
            this.selectedSlot = null;
        },

        async onSaveToSlot(slot) {
            const enhancedNodes = this.nodes.map(node => {
                return {
                    ...node,
                    outputData: node.outputData || '',
                    inputData: node.inputData || []
                };
            });
            const state = {
                nodes: enhancedNodes,
                lines: this.lines,
            };

            // Serialize and compress the state
            const serializedState = JSON.stringify(state);
            const compressed = pako.deflate(serializedState);

            try {
                // Save to IndexedDB
                await saveToIndexedDB(slot, compressed);
                this.onCloseSaveDialog();
                toast.success(`Problem compressed and saved to Slot ${slot}!`);
            } catch (error) {
                console.error('Failed to save problem:', error);
                toast.error('Failed to save problem.');
            }
        },

        async onLoadFromSlot(slot) {
            try {
                // Load from IndexedDB
                const compressed = await loadFromIndexedDB(slot);
                if (compressed) {
                    // Decompress the data
                    const decompressed = pako.inflate(compressed, { to: 'string' });

                    // Parse the decompressed JSON
                    const state = JSON.parse(decompressed);

                    this.$emit('clear-state');
                    setTimeout(() => {
                        const nodesWithData = state.nodes.map(node => ({
                            ...node,
                            outputData: node.outputData || '',
                            inputData: node.inputData || []
                        }));
                        state.nodes = nodesWithData;
                        this.$emit('load-state', state);
                        this.onCloseLoadDialog();
                        toast.success(`Problem loaded from Slot ${slot}!`);
                    }, 50);
                } else {
                    toast.warning(`No saved problem found in Slot ${slot}.`);
                }
            } catch (error) {
                console.error('Failed to load problem:', error);
                toast.error('Failed to load problem.');
            }
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

.dialog-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.dialog {
    background-color: white;
    padding: 20px;
    border-radius: 5px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    max-width: 400px;
    width: 100%;
}

.dialog h3 {
    margin-top: 0;
}

.save-slots {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
}

.save-slots button {
    flex: 1;
}

ul {
    list-style-type: none;
    padding: 0;
}

ul li {
    padding: 10px;
    cursor: pointer;
    border-bottom: 1px solid #eee;
}

ul li:hover {
    background-color: #f5f5f5;
}

.active {
    background-color: #3aa876;
    color: white;
}
</style>