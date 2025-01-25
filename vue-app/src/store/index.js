import { createStore } from 'vuex';

export default createStore({
  state: {
    nodes: [], // Global state for nodes
    lines: [], // Global state for lines (connections)
  },
  mutations: {
    /**
     * Mutation to update the nodes array in the state.
     * @param {object} state - The Vuex state object.
     * @param {Array} nodes - The new nodes array.
     */
    setNodes(state, nodes) {
      state.nodes = nodes;
    },

    /**
     * Mutation to update the lines array in the state.
     * @param {object} state - The Vuex state object.
     * @param {Array} lines - The new lines array.
     */
    setLines(state, lines) {
      state.lines = lines;
    },

    /**
     * Mutation to add a new line to the lines array.
     * @param {object} state - The Vuex state object.
     * @param {object} newLine - The new line to add.
     */
    addLine(state, newLine) {
      state.lines.push(newLine);
    },
  },
  actions: {
    /**
     * Action to update the nodes array in the state.
     * @param {object} context - The Vuex context object.
     * @param {Array} nodes - The new nodes array.
     */
    updateNodes({ commit }, nodes) {
      commit('setNodes', nodes);
    },

    /**
     * Action to update the lines array in the state.
     * @param {object} context - The Vuex context object.
     * @param {Array} lines - The new lines array.
     */
    updateLines({ commit }, lines) {
      commit('setLines', lines);
    },

    /**
     * Action to add a new line to the lines array.
     * @param {object} context - The Vuex context object.
     * @param {object} newLine - The new line to add.
     */
    addLine({ commit }, newLine) {
      commit('addLine', newLine);
    },
  },
  getters: {
    /**
     * Getter to access the nodes array from the state.
     * @param {object} state - The Vuex state object.
     * @returns {Array} - The nodes array.
     */
    nodes: (state) => state.nodes,

    /**
     * Getter to access the lines array from the state.
     * @param {object} state - The Vuex state object.
     * @returns {Array} - The lines array.
     */
    lines: (state) => state.lines,
  },
});