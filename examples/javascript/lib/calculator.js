calculator = {
    push: function(n) {
        if (!this.stack) this.stack = [];
        this.stack.push(n);
    },

    divide: function() {
        return this.stack[0] / this.stack[1];
    }
}
