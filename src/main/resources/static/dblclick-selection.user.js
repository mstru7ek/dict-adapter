// ==UserScript==
// @name         dblclick-selection
// @namespace    http://tampermonkey.net/
// @version      0.1
// @description  dictionary preview dblclick
// @author       You
// @match        http://*/*
// @match        https://*/*
// @require      https://cdnjs.cloudflare.com/ajax/libs/mustache.js/2.3.2/mustache.js
// @require      https://code.jquery.com/jquery-3.3.1.slim.min.js
// @grant        unsafeWindow
// @grant        GM_xmlhttpRequest
// @grant        GM_log
// @grant        GM_getValue
// @grant        GM_setValue

// ==/UserScript==



var template = `
        <div class="tile">
            <div class="tile-content">
                <h2 class="tile-query">{{title}}</h2>
                {{#providers}}
                <div class="tile-section">
                    <div class="tile-section-header">
                        <h3>{{{providerId}}}</h3>
                        {{#urlAudioStreams}}
                            <button class="tile-audio" data-url="{{url}}"> &raquo; </button>
                        {{/urlAudioStreams}}
                    </div>
                    <div class="tile-section-body">
                        <p class="description">{{description}}</p>

                        <h4>Definitions : </h4>
                        {{#definitions}}
                            <span class="def"> {{.}}</span>
                        {{/definitions}}
                        <h4>Synonyms : </h4>
                        {{#synonyms}}
                            <span class="syn"> {{.}} </span>
                        {{/synonyms}}
                    </div>
                </div>
                {{/providers}}
                <div class="tile-close">
                    <a href="#"> Close </a>
                </div>
            </div>
        </div>
`;

var cssStyle = `

.tile {
    background-color: #FFC;
}

.tile .tile-content {
    width: 40rem;
    max-width: 50rem;
    overflow-y: scroll;
    max-height: 500px;
    z-index: 1000;
    padding: 0.7rem;
}

.tile .tile-section {

    border-color: #CCC;
    border-style: solid;
    border-width: 1px 0px 0px 0px;

    margin: 0.8rem 0rem;
}

.tile h2.tile-query {
    margin: 0rem;
    font-weight: 600;
}


.tile-section-header h3 {
    display: inline-block;
    margin: 1rem 2rem 0rem 0rem;
    color: #999;
    font-style: italic;
    font-weight: 200;
    font-size: 1rem;
}

.tile-section-body h4 {
    margin: 1rem 2rem 0.5rem 0rem;
    color: #999;
    font-style: italic;
    font-weight: 200;
    font-size: 1rem;
}

.tile-content button.tile-audio {
    background-color: #FFE4C4;
    border: #000 1px solid;
    padding: 0.5rem 0.5rem;
}

.tile-content span.def {
    margin: 0.4rem 1rem 0rem 0rem;
    background-color: #AFF;

    display: block;
}

.tile-content span.syn {
    margin: 0rem 1rem 0rem 0rem;
    background-color: #AFA;
}

.tile-content .tile-close {
    margin: 2rem 0rem 0rem 0rem;
}

.tile-content .tile-close a {
    text-decoration: none;
}

`;

(function() {
    'use strict';


    function findByQuery(query) {
        return new Promise((resolve, reject) => {

            var requestHandle = GM_xmlhttpRequest({
                method: "GET",
                url: "http://localhost:8080/api?query=" + query,
                headers: {
                    "Content-Type": "application/json",
                    "Origin": "*"
                },
                onload: function(res) {
                    GM_log(res);
                    if(res.status !== 201) {
                        GM_log('Reponse-status:' + res.status);
                        resolve(res.responseText);
                    }
                },
                onerror: function(res) {
                    var msg = "An error occurred."
                    + "\nresponseText: " + res.responseText
                    + "\nreadyState: " + res.readyState
                    + "\nresponseHeaders: " + res.responseHeaders
                    + "\nstatus: " + res.status
                    + "\nstatusText: " + res.statusText
                    + "\nfinalUrl: " + res.finalUrl;
                    GM_log(msg);
                    console.log(msg);
                    reject(res.statusText);
                }
            });
        });
    }

    $(function() {
        console.log( "ready!" );

        $('head').append("<style>" + cssStyle + "</style>");

        window.addEventListener('dblclick', function (e) {
            console.log(e);
            var selection = window.getSelection().toString()

            findByQuery(selection).then(function (result) {
                var json = JSON.parse(result)
                var index = 0 ;
                var viewModel = {
                    title: selection,
                    providers: json
                }

                var output = Mustache.render(template , viewModel);

                $( "body" ).append( '<div id="block-wrapper" style="position: absolute;z-index: 1000"></div>' );

                $( "#block-wrapper" ).html(output);

                // position at center
                var viewportWidth = $(document).width();
                var viewportHeight = $(document).height();

                $( "#block-wrapper" ).css({ top: (e.pageY - $( "#block-wrapper" ).height()/2) + "px" , left: (viewportWidth/2 -  $( "#block-wrapper" ).width()/2) + "px" });

                $("button.tile-audio").click(function() {
                    var url = $(this).attr('data-url');
                    var audio = new Audio(url);
                    audio.play();
                });

                $(document).mouseup(function(e) {
                    var container = $("#block-wrapper");
                    if (!container.is(e.target) && container.has(e.target).length === 0) {
                        container.hide();
                        container.remove();
                    }
                });

                $(".tile-close a").click(function() {
                    $("#block-wrapper").remove();
                });

            }).catch(function (status) {
                console.log(status)
            });

        });

    });

})();