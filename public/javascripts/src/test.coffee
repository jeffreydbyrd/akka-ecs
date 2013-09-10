dims=
        w:200
        h:200
        
#Set the stage:
stage = new Kinetic.Stage({
        container: 'container',
        width: dims.w,
        height: dims.h
});

# Surface Layer:
surfaces = new Kinetic.Layer();
testFloor = new Kinetic.Line({
        points:[
                0,  dims.h-0
                200,dims.h-200 
        ]
        stroke:'blue'
        strokeWidth: 1
});
surfaces.add(testFloor)
stage.add(surfaces)

# Mobiles Layer        
mobiles = new Kinetic.Layer();

# A little test player
plr = new Kinetic.Rect({
        x: 100
        y: 100
        width: 2
        height: 4
        fill: 'black'
        stroke: 'black'
        strokeWidth: 1
});

mobiles.add(plr);
stage.add(mobiles);

move = (pos) ->
        plr.setX(pos.x)
        plr.setY(dims.h - pos.y)
        stage.draw()

wsUrl = "ws://127.0.0.1:9000/test?username=jb"
ws = new WebSocket(wsUrl)

ws.onopen = (evt) ->
        console.log("websocket opened")

ws.onmessage = (evt) ->
        pos = JSON.parse(evt.data)
        move(pos)

onkey = (evt) ->
        cmd = { type : evt.type, data : evt.keyCode }
        ws.send(JSON.stringify(cmd))

body = document.getElementById('body')
body.onkeydown = onkey
body.onkeyup = onkey
