class RiBuHiLi:
    def __init__(self, s):
        self.size = s
        self.buffer = [None] * self.size
        self.position = 0

    def init(self):
        self.position = 0
        for i in range(self.size):
            self.buffer[i] = None

    def push(self, item):
        new_pos = self.position % self.size
        self.position += 1
        if 0 <= new_pos < self.size:
            self.buffer[new_pos] = item
            # print(f"inserting <{item}> at {new_pos}")
        else:
            print(f"inserting at {new_pos} not possible!")

    def get_buffer(self):
        fifo = []
        for i in range(self.position + self.size - 1, self.position - 1, -1):
            if self.buffer[i % self.size] is not None:
                fifo.append(self.buffer[i % self.size])
        return fifo