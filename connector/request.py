class Request():
	PENDING = 0
	MOTION = 1
	COMPLETE = 2
	COKE = 0
	WATER = 1
	LEMONADE = 2
	status = PENDING
	
	def __init__(self):
		self.location = ""
		self.order = [0,0,0]
		self.currOrder = ""
		self.currLoc = ""
		self.currFav = ""
		self.ready = False
		self.status = self.PENDING
		self.context = ""
		
	def setOrder(self,currOrder):
		currOrder = currOrder.lower()
		if currOrder == "coke":
			self.order[Request.COKE] = 1
		elif currOrder == "water":
			self.order[Request.WATER] = 1
		elif currOrder == "lemonade":
			self.order[Request.LEMONADE] = 1		
	
	def isReady(self):
		return self.location and (self.order[0] == 1 or self.order[1] == 1 or self.order[2] == 1)
		
