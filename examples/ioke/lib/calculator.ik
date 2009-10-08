
Calculator = Origin mimic do(
  stack = []

  pushNumber = method(num,
    stack << num
  )

  calculate = method(rator,
    right = stack pop!
    left  = stack pop!
    stack << case(rator,
      :+, left + right,
      :/, left / right)
  )

  currentValue = method(stack last)
)
