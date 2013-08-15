$(document).ready(  ->
  wsUrl = "ws://127.0.0.1:9000/test?username=jb"
  ws = new WebSocket(wsUrl)

  ws.onopen = (evt) ->
    console.log("websocket opened")

  ws.onmessage = (evt) ->
    console.log(evt.data)
    $('#xpos').html(evt.data)

  onkey = (evt) ->
    cmd = { type : evt.type, data : evt.keyCode }
    ws.send JSON.stringify cmd

  $('html').keydown onkey
  $('html').keyup onkey

  $(window).unload( (evt) ->
    evt.type = 'keyup'
    evt.keyCode = 81 # Q for quit
    onkey(evt);
  )
     
)
