package subcut.loadmodule

class SharedBetweenSteps {
	private var visited = false
	
	def visit()={
	  visited = true
	}
	
	def isVisited()=visited
}