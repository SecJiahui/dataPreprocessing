
class RingbufferHistory:
    def __init__(self, size):
        self.buffer = [None] * size
        self.size = size
        self.position = 0

    def init(self):
        self.position = 0
        for i in range(self.size):
            self.buffer[i] = None

    def push(self, item):
        new_pos = self.position % self.size
        if 0 <= new_pos < self.size:
            self.buffer[new_pos] = item
            self.position += 1
        else:
            print(f"Inserting at {new_pos} not possible!")

    def list(self):
        result = ""
        # Output in FiFo order
        for i in range(self.position + self.size - 1, self.position - 1, -1):
            if self.buffer[i % self.size] is not None:
                result += str(self.buffer[i % self.size]) + "\n"
            else:
                return ""
        return result
