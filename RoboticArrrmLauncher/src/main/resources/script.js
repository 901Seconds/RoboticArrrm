// Copyright 2010 William Malone (www.williammalone.com)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/*jslint browser: true */
/*global G_vmlCanvasManager */

var host = window.location.hostname;
var socket = io(host+":9092");
var drawingApp = (function () {

    "use strict";

    var canvas,
        context,
        canvasWidth = screen.width*2,
        canvasHeight = screen.height*2,
        clickX = [],
        clickY = [],
        clickDrag = [],
        paint = false,

        // Clears the canvas.
        clearCanvas = function () {
            context.clearRect(0, 0, canvasWidth, canvasHeight);
        },

        // Redraws the canvas.
        redraw = function () {
            var radius,i;
            clearCanvas();
            context.fillStyle = '#333333';
            context.fill();
            // For each point drawn
            for (i = 0; i < clickX.length; i += 1) {
                radius = 1;
                // Set the drawing path
                context.beginPath();
                // If dragging then draw a line between the two points
                if (clickDrag[i] && i) {
                    context.moveTo(clickX[i - 1], clickY[i - 1]);
                } else {
                    // The x position is moved over one pixel so a circle even if not dragging
                    context.moveTo(clickX[i] - 1, clickY[i]);
                }
                context.lineTo(clickX[i], clickY[i]);

                context.strokeStyle = "#000000";
                context.lineCap = "round";
                context.lineJoin = "round";
                context.lineWidth = radius;
                context.stroke();
            }
            context.closePath();
            context.globalAlpha = 1; // No IE support
        },

        // Adds a point to the drawing array.
        // @param x
        // @param y
        // @param dragging
        addClick = function (x, y, dragging) {
            var lastClickX = clickX[clickX.length-1];
            var lastClickY = clickY[clickY.length-1];
            if (!dragging) {
                lastClickX = x;
                lastClickY = y;
            }
            socket.emit("drawShape",{
                "xpoints":[lastClickX,x],
                "ypoints":[lastClickY,y],
                "penDown":!dragging
            });
            clickX.push(x);
            clickY.push(y);
            clickDrag.push(dragging);
        },

        createUserEvents = function () {

            var press = function (e) {
                    // Mouse down location
                    var mouseX = (e.changedTouches ? e.changedTouches[0].pageX : e.pageX) - this.offsetLeft,
                        mouseY = (e.changedTouches ? e.changedTouches[0].pageY : e.pageY) - this.offsetTop;
                    paint = true;
                    addClick(mouseX, mouseY, false);
                    redraw();
                },

                drag = function (e) {
                    var mouseX = (e.changedTouches ? e.changedTouches[0].pageX : e.pageX) - this.offsetLeft,
                        mouseY = (e.changedTouches ? e.changedTouches[0].pageY : e.pageY) - this.offsetTop;

                    if (paint) {
                        addClick(mouseX, mouseY, true);
                        redraw();
                    }
                    // Prevent the whole page from dragging if on mobile
                    e.preventDefault();
                },

                release = function () {
                    paint = false;
                    redraw();
                },

                cancel = function () {
                    paint = false;
                };

            // Add mouse event listeners to canvas element
            canvas.addEventListener("mousedown", press, false);
            canvas.addEventListener("mousemove", drag, false);
            canvas.addEventListener("mouseup", release);
            canvas.addEventListener("mouseout", cancel, false);

            // Add touch event listeners to canvas element
            canvas.addEventListener("touchstart", press, false);
            canvas.addEventListener("touchmove", drag, false);
            canvas.addEventListener("touchend", release, false);
            canvas.addEventListener("touchcancel", cancel, false);
        },


        // Creates a canvas element, loads images, adds events, and draws the canvas for the first time.
        init = function () {

            // Create the canvas (Neccessary for IE because it doesn't know what a canvas element is)
            canvas = document.createElement('canvas');
            canvas.setAttribute('width', canvasWidth);
            canvas.setAttribute('height', canvasHeight);
            canvas.setAttribute('id', 'canvas');
            document.getElementById('canvasDiv').appendChild(canvas);
            if (typeof G_vmlCanvasManager !== "undefined") {
                canvas = G_vmlCanvasManager.initElement(canvas);
            }
            context = canvas.getContext("2d"); // Grab the 2d canvas context
            // Note: The above code is a workaround for IE 8 and lower. Otherwise we could have used:
            //     context = document.getElementById('canvas').getContext("2d");

            createUserEvents();
        };

    return {
        init: init
    };
}());