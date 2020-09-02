
/* Language settings */
var langs = [['English', ['en-AU', 'AUS'],['en-US', 'US']],['Korean',['ko-KR','KOR']]];
var selectLanguage = document.getElementById("selectLanguage");
var selectDialect = document.getElementById("selectDialect");

var startButton = document.getElementById("startButton");

var startImg = document.getElementById("startImg");
var final_span = document.getElementById("final_span");
var interim_span= document.getElementById("interim_span");
var info = document.getElementById("info");
var window  = document.getElementById("window");
//var grammar = '#JSGF V1.0; grammar unitcode; public <unitcode> = KIT001| KIT101| KIT102| KIT103| KIT104| KIT106| KIT107| KIT201| KIT204| KIT206| KIT212| KIT302| KIT306| KIT309| KIT310| KIT312| KIT313| KIT401| KIT402| KIT403| KIT406| KIT409| KIT501| KIT506| KIT606| KIT613| KIT701| KIT702| KIT703| KIT708| KIT709| KIT712| KIT713| KIT714| KNE451| KNE999| KNE101| KNE111| KNE113| KNE216| KNE223| KNE251| KNE315| KNE333| KNE343| KNE345| KNE354| KNE356| KNE357| KNE373| KNE411| KNE412| KNE444| KNE432| KNE443| KNE446| KNE470| KNE773| KNE715| KNE347| KNE400| KNE231| KNE239| KNE240| KNE273| KNE312| KNE316| KNE334| KNE336| KNE346| KNE351| KNE372| KNE441| KNE434| KNE491| KNE454| KNE462| KNE457| KNE314| KNE488| KNE486| KNE342| KNE726| KNE744| KNE787| KNE151| KNE122| KNE210| KNE211| KNE213| KNE222| KNE712| KNE751 ;';
var grammar = '#JSGF V1.0; grammar alphabet; public <alphabet> = a | b | c | d | e | f | g | h | i | j | k | l | m | n | o | p | q | r | s | t | u | v | w | x | y | z;';

var SpeechRecognition = SpeechRecognition || webkitSpeechRecognition;
var SpeechGrammarList = SpeechGrammarList || webkitSpeechGrammarList;

for (var i = 0; i < langs.length; i++) {
    //document.getElementById("selectLanguage").options[i] = new Option(langs[i][0], i);
    var option = document.createElement('option');
    option.text = langs[i][0];
    option.value = i;
    selectLanguage.add(option);
}

document.getElementById("selectLanguage").selectedIndex = 0;
updateCountry();

selectDialect.selectedIndex = 0;
showInfo('info_start');

var speechRecognitionList = new SpeechGrammarList();
speechRecognitionList.addFromString(grammar, 1);


function updateCountry() {
  for (var i = selectDialect.options.length - 1; i >= 0; i--) {
    selectDialect.remove(i);
  }
  var list = langs[selectLanguage.selectedIndex];
  for (var i = 1; i < list.length; i++) {
    selectDialect.options.add(new Option(list[i][1], list[i][0]));
  }
  selectDialect.style.visibility = list[1].length === 1 ? 'hidden' : 'visible';
}

var create_email = false;
var final_transcript = '';
var recognizing = false;
var ignore_onend;
var start_timestamp;

if (!('SpeechRecognition' in window)) {
  alert("speech not supported by browser..");
  upgrade();
} else {
  startButton.style.display = 'inline-block';
  var recognition = new SpeechRecognition();
  recognition.continuous = false;
  recognition.interimResults = true;

  recognition.onstart = function() {
    recognizing = true;
    showInfo('info_speak_now');
    startImg.src = 'mic-animate.gif';
    prev_final_transcript = final_transcript;
    final_transcript = "";
    $('#final_span').html("");
  };
  
  
  recognition.onerror = function(event) {
    if (event.error == 'no-speech') {
      startImg.src = 'mic.gif';
      showInfo('info_no_speech');
      ignore_onend = true;
      alert("Error");
    }
    
    if (event.error == 'audio-capture') {
      startImg.src = 'mic.gif';
      showInfo('info_no_microphone');
      ignore_onend = true;
    }
    
    if (event.error == 'not-allowed') {
      if (event.timeStamp - start_timestamp < 100) {
        showInfo('info_blocked');
      } else {
        showInfo('info_denied');
      }
      ignore_onend = true;
    }
  };
  
  recognition.onend = function() {
    recognizing = false;
    
    if (ignore_onend) {
      return;
    }
    startImg.src = 'mic.gif';
    if (!final_transcript) {
      showInfo('info_start');
      return;
    }
    
    showInfo('');
    if (window.getSelection) {
      window.getSelection().removeAllRanges();
      var range = document.createRange();
      range.selectNode(document.getElementById('final_span'));
      window.getSelection().addRange(range);
    } 
    
    // final_transcript = preProcessDialog(final_transcript,);  DPH June 2019
    preProcessDialog(final_transcript,processUserInputCallback);
    //processDialog(final_transcript); DPH June 2019
    final_transcript = "";
    $('#final_span').html("");

  };
  
var prev_final_transcript;

setInterval(function(){ 
    if(recognizing){
        if(prev_final_transcript == final_transcript){

        } else {
            final_transcript = preProcessDialog(final_transcript);
            processDialog(final_transcript);
            final_transcript = "";
            $('#final_span').html("");
        }
    }
}, 1000);
    
  recognition.onresult = function(event) {
      
    var interim_transcript = '';
    for (var i = event.resultIndex; i < event.results.length; ++i) {
      if (event.results[i].isFinal) {
        final_transcript += event.results[i][0].transcript;
      } else {
        interim_transcript += event.results[i][0].transcript;
      }
    }
    //final_transcript = capitalize(final_transcript);
    final_span.innerHTML = linebreak(final_transcript);
    interim_span.innerHTML = linebreak(interim_transcript);
    if (final_transcript || interim_transcript) {
      showButtons('inline-block');
    }
  };
}

function upgrade() {
  startButton.style.visibility = 'hidden';
  showInfo('info_upgrade');
}

var two_line = /\n\n/g;
var one_line = /\n/g;

function linebreak(s) {
  return s.replace(two_line, '<p></p>').replace(one_line, '<br>');
}

var first_char = /\S/;

function capitalize(s) {
  return s.replace(first_char, function(m) { return m.toUpperCase(); });
}

function startSpeechRecognition() {
  if (recognizing) {
    recognition.stop();
    return;
  }
  final_transcript = '';
  recognition.lang = selectDialect.value;
  recognition.start();
  ignore_onend = false;
  final_span.innerHTML = '';
  interim_span.innerHTML = '';
  startImg.src = 'mic-slash.gif';
  showInfo('info_allow');
  showButtons('none');
  start_timestamp = event.timeStamp;
  prev_timestamp = event.timeStamp;
}

function showInfo(s) {
  if (s) {
    for (var child = info.firstChild; child; child = child.nextSibling) {
      if (child.style) {
        child.style.display = child.id == s ? 'inline' : 'none';
      }
    }
    info.style.visibility = 'visible';
  } else {
    info.style.visibility = 'hidden';
  }
}
var current_style;
function showButtons(style) {
  if (style == current_style) {
    return;
  }
  current_style = style;
}

// System TTS
function tts(responseText) {
    voices = window.speechSynthesis.getVoices();
    
    var u1 = new SpeechSynthesisUtterance(responseText);
    //u1.lang = 'en-US';
    u1.lang = selectDialect.value;
    u1.pitch = 1;
    u1.rate = 1;
    //alert("Lang is set to " + selectDialect.value);

    for (i = 0; i < voices.length; i++) {
        if (voices[i].lang === u1.lang) {
            u1.voice = voices[i];
            //alert("Lang is set to voices[" + i + "]");
            break;
        }
        //alert("Voice " + i + " is " + voices[i].name + "(" + voices[i].lang + ")");
    }
    //u1.voice = voices[45]; // u1.voice = voices[3]; for man
    u1.voiceURI = 'native';
    u1.volume = 1;
    speechSynthesis.speak(u1);
   
}

function stopTts() {
     speechSynthesis.cancel();
}