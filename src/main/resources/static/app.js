const $=s=>document.querySelector(s);let me=null,tests=[],questions=[],active=null,answers={},at=0,left=0,tick=null,mode="login";
// API client: use the Spring Boot backend when available; otherwise keep the UI usable in local demo mode.
let demoMode=false;
const DEMO_TESTS=[
 {ID:1,TITLE:"Java Fundamentals",CATEGORY:"Programming",DESCRIPTION:"Core Java, OOP and language fundamentals",DURATION_MINUTES:15,QUESTION_COUNT:10},
 {ID:2,TITLE:"Data Structures & Algorithms",CATEGORY:"Computer Science",DESCRIPTION:"Arrays, searching, sorting and trees",DURATION_MINUTES:15,QUESTION_COUNT:10},
 {ID:3,TITLE:"Database Management",CATEGORY:"Computer Science",DESCRIPTION:"SQL, keys, normalization and transactions",DURATION_MINUTES:15,QUESTION_COUNT:10},
 {ID:4,TITLE:"HTML & CSS",CATEGORY:"Web Development",DESCRIPTION:"Markup, styling and responsive layouts",DURATION_MINUTES:15,QUESTION_COUNT:10},
 {ID:5,TITLE:"JavaScript Essentials",CATEGORY:"Web Development",DESCRIPTION:"Variables, functions and DOM basics",DURATION_MINUTES:15,QUESTION_COUNT:10},
 {ID:6,TITLE:"Python Programming",CATEGORY:"Programming",DESCRIPTION:"Python syntax, collections and functions",DURATION_MINUTES:15,QUESTION_COUNT:10},
 {ID:7,TITLE:"Computer Networks",CATEGORY:"Computer Science",DESCRIPTION:"Protocols and networking concepts",DURATION_MINUTES:15,QUESTION_COUNT:10},
 {ID:8,TITLE:"Operating Systems",CATEGORY:"Computer Science",DESCRIPTION:"Processes, memory and scheduling",DURATION_MINUTES:15,QUESTION_COUNT:10},
 {ID:9,TITLE:"Aptitude & Reasoning",CATEGORY:"Aptitude",DESCRIPTION:"Logical and quantitative reasoning",DURATION_MINUTES:15,QUESTION_COUNT:10},
 {ID:10,TITLE:"Cyber Security Basics",CATEGORY:"Computer Science",DESCRIPTION:"Security principles and safe computing",DURATION_MINUTES:15,QUESTION_COUNT:10}
];
const DEMO_QUESTIONS=[
 ["Which method is the entry point of a Java application?","start()","run()","main()","init()","C"],
 ["Which keyword is used to inherit a class in Java?","implements","extends","inherits","super","B"],
 ["Which data structure follows FIFO?","Stack","Queue","Tree","Graph","B"],
 ["What is the time complexity of binary search?","O(n)","O(log n)","O(n²)","O(1)","B"],
 ["Which SQL command retrieves rows?","GET","SELECT","OPEN","FETCH TABLE","B"],
 ["Which key uniquely identifies a table row?","Foreign key","Primary key","Index","Composite value","B"],
 ["Which HTML element creates a hyperlink?","<link>","<a>","<href>","<url>","B"],
 ["Which CSS property changes text color?","font-style","background","color","text-size","C"],
 ["Which language adds interactivity to web pages?","SQL","JavaScript","XML","C","B"],
 ["Which protocol securely transfers web pages?","HTTP","FTP","HTTPS","SMTP","C"]
];
function demoRead(key,fallback){try{return JSON.parse(localStorage.getItem("examvault_"+key))??fallback}catch(e){return fallback}}
function demoWrite(key,value){localStorage.setItem("examvault_"+key,JSON.stringify(value))}
function demoQuestions(examId){return Array.from({length:10},(_,i)=>{const q=DEMO_QUESTIONS[(examId+i-1)%DEMO_QUESTIONS.length];return {ID:examId*100+i+1,QUESTION_TEXT:q[0],OPTION_A:q[1],OPTION_B:q[2],OPTION_C:q[3],OPTION_D:q[4],CORRECT_OPTION:q[5]}})}
function localDemoApi(path,opt={}){
 demoMode=true;
 const method=(opt.method||"GET").toUpperCase();
 let body={};try{body=opt.body?JSON.parse(opt.body):{}}catch(e){}
 const users=demoRead("users",[]);
 let session=demoRead("session",null);
 const current=()=>session?{id:session.id,name:session.name,email:session.email,role:session.role}:null;
 if(path==="/auth/signup"&&method==="POST"){
  const email=String(body.email||"").trim().toLowerCase();
  if(!body.name||!email||!String(body.password||"")||!body.phone||!body.age)throw Error("Please complete all registration fields.");
  if(users.some(u=>u.email===email))throw Error("An account with this email already exists.");
  const u={id:Date.now(),name:String(body.name).trim(),email,phone:body.phone,age:body.age,password:body.password,role:"STUDENT"};
  users.push(u);demoWrite("users",users);session={id:u.id,name:u.name,email:u.email,role:u.role};demoWrite("session",session);return current();
 }
 if(path==="/auth/login"&&method==="POST"){
  const email=String(body.email||"").trim().toLowerCase();
  let u=users.find(x=>x.email===email);
  if(email==="admin@examvault.com"&&body.password==="Admin@123")u={id:0,name:"ExamVault Admin",email,role:"ADMIN"};
  if(!u||u.password!==body.password)throw Error("Incorrect email or password.");
  if(body.portal==="ADMIN"&&u.role!=="ADMIN")throw Error("This account is not an administrator account.");
  if(body.portal!=="ADMIN"&&u.role!=="STUDENT")throw Error("Please use administrator sign-in.");
  session={id:u.id,name:u.name,email:u.email,role:u.role};demoWrite("session",session);return current();
 }
 if(path==="/auth/me"){if(!current())throw Error("Please sign in.");return current()}
 if(path==="/auth/logout"&&method==="POST"){localStorage.removeItem("examvault_session");return {message:"Signed out"}}
 if(path==="/exams"){if(!current())throw Error("Please sign in.");return DEMO_TESTS}
 if(path==="/results"){if(!current())throw Error("Please sign in.");return demoRead("results",[]).filter(x=>x.USER_ID===session.id)}
 const qm=path.match(/^\/exams\/(\d+)\/questions$/);
 if(qm){if(!current())throw Error("Please sign in.");const id=Number(qm[1]),exam=DEMO_TESTS.find(x=>x.ID===id);if(!exam)throw Error("Test not found.");return {exam,questions:demoQuestions(id)}}
 if(path==="/attempts"&&method==="POST"){
  if(!current())throw Error("Please sign in.");
  const exam=DEMO_TESTS.find(x=>x.ID===Number(body.examId));if(!exam)throw Error("Test not found.");
  const qs=demoQuestions(exam.ID);const score=qs.filter(q=>body.answers?.[String(q.ID)]===q.CORRECT_OPTION).length;
  const result={ID:Date.now(),USER_ID:session.id,TITLE:exam.TITLE,CATEGORY:exam.CATEGORY,SCORE:score,TOTAL:qs.length,PERCENTAGE:score*10,SUBMITTED_AT:new Date().toISOString()};
  const rows=demoRead("results",[]);rows.unshift(result);demoWrite("results",rows);
  return {score,total:qs.length,percentage:score*10,message:score>=4?"Well done! Keep it up.":"Keep practising. You can improve!"};
 }
 if(path==="/admin/questions"&&method==="POST"){
  if(!current()||session.role!=="ADMIN")throw Error("Administrator access required.");
  const rows=demoRead("extra_questions",[]);rows.push(body);demoWrite("extra_questions",rows);return {message:"Question added in this browser demo."};
 }
 throw Error("This action is not available in demo mode.");
}
async function api(path,opt={}){
 try{
  const r=await fetch("/api"+path,{credentials:"same-origin",headers:{"Content-Type":"application/json"},...opt});
  let d={};try{d=await r.json()}catch(e){}
  if(!r.ok){
   if([404,500,502,503,504].includes(r.status))return localDemoApi(path,opt);
   throw Error(d.error||"Request failed");
  }
  return d;
 }catch(e){
  if(e instanceof TypeError||/Failed to fetch|NetworkError|Load failed/i.test(e.message))return localDemoApi(path,opt);
  throw e;
 }
}
function toast(m){let t=$("#toast");t.textContent=m;t.classList.add("show");setTimeout(()=>t.classList.remove("show"),2800)}
function authMode(m){mode=m;let sign=m==="signup",admin=m==="admin";$("#nameLabel").classList.toggle("hidden",!sign);$("#phoneLabel").classList.toggle("hidden",!sign);$("#ageLabel").classList.toggle("hidden",!sign);$("#name").required=sign;$("#phone").required=sign;$("#age").required=sign;$("#authEyebrow").textContent=sign?"CREATE YOUR ACCOUNT":admin?"ADMINISTRATOR ACCESS":"WELCOME BACK";$("#authTitle").innerHTML=sign?"A fresh start.<br>A stronger you.":admin?"Welcome, administrator.":"Sign in to your<br>learning space.";$("#authSub").textContent=sign?"Set up your space and start making progress.":admin?"Sign in to manage tests and the question bank.":"Your next best score is one session away.";$("#authButton").innerHTML=sign?"Create account　→":admin?"Admin sign in　→":"Sign in　→";$("#switchText").innerHTML=admin?'Administrator portal · <button id="switch">Student sign in</button>':sign?'Already a member? <button id="switch">Sign in</button>':'New here? <button id="switch">Create account</button>';$("#switch").onclick=()=>authMode(mode==="signup"?"login":"login");$("#adminSwitch").classList.toggle("hidden",admin);$("#adminSwitch").textContent="Admin sign in";$("#switchText").classList.toggle("hidden",admin&&false)}
$("#switch").onclick=()=>authMode("signup");
$("#adminSwitch").onclick=()=>authMode(mode==="admin"?"login":"admin");
$("#authForm").onsubmit=async e=>{e.preventDefault();try{let body={email:$("#email").value,password:$("#password").value,portal:mode==="admin"?"ADMIN":"STUDENT"};if(mode==="signup"){body.name=$("#name").value;body.phone=$("#phone").value;body.age=$("#age").value}me=await api(mode==="signup"?"/auth/signup":"/auth/login",{method:"POST",body:JSON.stringify(body)});showApp()}catch(x){toast(x.message)}};
function esc(s){return String(s??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]))}
async function showApp(){$("#auth").classList.add("hidden");$("#app").classList.remove("hidden");$("#userName").textContent=me.name;$("#welcome").textContent=me.name.split(" ")[0];$("#userRole").textContent=me.role+" ACCOUNT";$("#avatar").textContent=me.name[0].toUpperCase();document.querySelectorAll(".admin").forEach(x=>x.classList.toggle("hidden",me.role!=="ADMIN"));await load();go("dashboard")}
async function load(){tests=await api("/exams");let rows=await api("/results");$("#statTests").textContent=String(tests.length).padStart(2,"0");$("#statDone").textContent=rows.length;$("#statAvg").textContent=rows.length?Math.round(rows.reduce((a,r)=>a+Number(r.PERCENTAGE),0)/rows.length)+"%":"—";let cards=tests.map(card).join("");$("#allTests").innerHTML=cards;$("#featured").innerHTML=tests.slice(0,3).map(card).join("");document.querySelectorAll("[data-test]").forEach(b=>b.onclick=()=>start(Number(b.dataset.test)));$("#qExam").innerHTML=tests.map(t=>'<option value="'+t.ID+'">'+esc(t.TITLE)+'</option>').join("");renderResults(rows)}
function card(t,i){let colors=["blue","orange","violet","green","rose"];return '<article class="test-card '+colors[i%5]+'"><div class="card-top"><small>'+esc(t.CATEGORY)+'</small><span>0'+(i+1)+'</span></div><div class="symbol">'+["⌘","⌬","▦","✳","◎"][i%5]+'</div><h3>'+esc(t.TITLE)+'</h3><p>'+esc(t.DESCRIPTION)+'</p><div class="meta">◷ '+t.DURATION_MINUTES+' min　 ·　 ▤ '+t.QUESTION_COUNT+' questions</div><button class="card-action" data-test="'+t.ID+'">Start practice <span>→</span></button></article>'}
function go(p){document.querySelectorAll(".page").forEach(x=>x.classList.add("hidden"));$("#"+p).classList.remove("hidden");document.querySelectorAll(".nav").forEach(x=>x.classList.toggle("active",x.dataset.page===p));let labels={dashboard:["OVERVIEW","Your learning overview"],tests:["PRACTICE LIBRARY","Explore all tests"],results:["MY RESULTS","Your results"],admin:["QUESTION BANK","Manage questions"]};$("#crumb").textContent=labels[p][0];$("#pageTitle").textContent=labels[p][1];window.scrollTo(0,0)}
document.querySelectorAll("[data-page]").forEach(b=>b.onclick=()=>go(b.dataset.page));document.querySelectorAll("[data-go]").forEach(b=>b.onclick=()=>go(b.dataset.go));
function renderResults(rows){$("#resultsList").innerHTML=rows.length?rows.map(r=>'<article class="result-row"><i>✓</i><div><b>'+esc(r.TITLE)+'</b><small>'+esc(r.CATEGORY)+' · '+new Date(r.SUBMITTED_AT).toLocaleString()+'</small></div><strong>'+Number(r.PERCENTAGE).toFixed(0)+'%<small>'+r.SCORE+'/'+r.TOTAL+' correct</small></strong><span class="badge">'+(r.PERCENTAGE>=40?"Completed":"Keep practising")+'</span></article>').join(""):'<div class="empty"><h3>Your story starts here.</h3><p>Complete your first test and your results will appear here.</p><button class="primary" data-go="tests">Explore tests →</button></div>'}
async function start(id){try{let d=await api("/exams/"+id+"/questions");active=d.exam;questions=d.questions;answers={};at=0;left=Number(active.DURATION_MINUTES)*60;$("#testTitle").textContent=active.TITLE;$("#testModal").classList.remove("hidden");renderQ();clock();tick=setInterval(()=>{left--;clock();if(left<=0){clearInterval(tick);finish(true)}},1000)}catch(e){toast(e.message)}}
function clock(){$("#timer").textContent=String(Math.floor(left/60)).padStart(2,"0")+":"+String(left%60).padStart(2,"0");$("#timer").classList.toggle("urgent",left<60)}
function renderQ(){let q=questions[at];$("#testProgress").textContent="Question "+(at+1)+" of "+questions.length;$("#bar").style.width=((at+1)/questions.length*100)+"%";let html='<div class="qcount">QUESTION '+String(at+1).padStart(2,"0")+'</div><h3 class="qtext">'+esc(q.QUESTION_TEXT)+'</h3><div class="options">';["A","B","C","D"].forEach(k=>{html+='<button class="option '+(answers[q.ID]===k?"selected":"")+'" data-answer="'+k+'"><b>'+k+'</b><span>'+esc(q["OPTION_"+k])+'</span><i>'+(answers[q.ID]===k?"✓":"")+'</i></button>'});$("#questionArea").innerHTML=html+'</div>';document.querySelectorAll("[data-answer]").forEach(b=>b.onclick=()=>{answers[q.ID]=b.dataset.answer;renderQ()});$("#prev").disabled=at===0;$("#next").classList.toggle("hidden",at===questions.length-1);$("#submit").classList.toggle("hidden",at!==questions.length-1)}
$("#next").onclick=()=>{if(at<questions.length-1){at++;renderQ()}};$("#prev").onclick=()=>{if(at>0){at--;renderQ()}};$("#quit").onclick=()=>{if(confirm("Exit this test? Your answers will not be submitted.")){clearInterval(tick);$("#testModal").classList.add("hidden")}};$("#submit").onclick=()=>finish(false);
async function finish(auto){if(!active)return;clearInterval(tick);$("#testModal").classList.add("hidden");try{let r=await api("/attempts",{method:"POST",body:JSON.stringify({examId:active.ID,answers})});$("#resultPercent").textContent=Math.round(r.percentage)+"%";$("#resultScore").textContent=r.score+" / "+r.total+" correct answers";$("#resultMessage").textContent=(auto?"Time is up. Your answers were submitted automatically. ":"")+r.message;$("#resultModal").classList.remove("hidden");await load()}catch(e){toast(e.message)}}
$("#closeResult").onclick=$("#resultDone").onclick=()=>{$("#resultModal").classList.add("hidden");go("dashboard")};
$("#questionForm").onsubmit=async e=>{e.preventDefault();try{await api("/admin/questions",{method:"POST",body:JSON.stringify({examId:$("#qExam").value,question:$("#qText").value,a:$("#qA").value,b:$("#qB").value,c:$("#qC").value,d:$("#qD").value,correct:$("#qCorrect").value,explanation:$("#qExplanation").value})});e.target.reset();toast("Question added successfully.");await load()}catch(x){toast(x.message)}};
$("#logout").onclick=async()=>{await api("/auth/logout",{method:"POST"});location.reload()};
(async()=>{try{me=await api("/auth/me");showApp()}catch(e){}})();