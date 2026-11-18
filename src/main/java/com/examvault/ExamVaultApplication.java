package com.examvault;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import jakarta.servlet.http.HttpSession;
import java.util.*;

@SpringBootApplication
public class ExamVaultApplication {
 public static void main(String[] args){SpringApplication.run(ExamVaultApplication.class,args);}
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean CommandLineRunner seed(JdbcTemplate db,PasswordEncoder encoder){return args->{
  db.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20)");
  db.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS age INT");
  {
   String[][] tests={{"Java Fundamentals","Programming","Core Java, OOP and language fundamentals","15"},{"Data Structures & Algorithms","Computer Science","Arrays, searching, sorting and trees","15"},{"Database Management","Computer Science","SQL, keys, normalization and transactions","15"},{"HTML & CSS","Web Development","Markup, styling and responsive layouts","15"},{"JavaScript Essentials","Web Development","Variables, functions and DOM basics","15"},{"Python Programming","Programming","Python syntax, collections and functions","15"},{"Computer Networks","Computer Science","Protocols and networking concepts","15"},{"Operating Systems","Computer Science","Processes, memory and scheduling","15"},{"Aptitude & Reasoning","Aptitude","Logical and quantitative reasoning","15"},{"Cyber Security Basics","Computer Science","Security principles and safe computing","15"}};
   for(String[] t:tests)if(db.queryForList("SELECT id FROM exams WHERE title=?",t[0]).isEmpty())db.update("INSERT INTO exams(title,category,description,duration_minutes) VALUES(?,?,?,?)",t[0],t[1],t[2],Integer.parseInt(t[3]));
   String[][] q={
    {"Which method is the entry point of a Java application?","start()","run()","main()","init()","C"},{"Which keyword is used to inherit a class in Java?","implements","extends","inherits","super","B"},{"Which concept allows one interface to have many implementations?","Encapsulation","Polymorphism","Compilation","Casting","B"},{"Which type stores true or false?","int","char","boolean","float","C"},{"Which keyword prevents a method from being overridden?","static","final","private","this","B"},
    {"Which data structure follows FIFO?","Stack","Queue","Tree","Graph","B"},{"What is the time complexity of binary search on a sorted array?","O(n)","O(log n)","O(n²)","O(1)","B"},{"Which structure uses nodes with left and right child links?","Queue","Binary tree","Hash table","Stack","B"},{"Which sorting algorithm repeatedly selects the minimum element?","Merge sort","Quick sort","Selection sort","Heap sort","C"},{"Which structure is best suited for LIFO operations?","Queue","Stack","Array only","Graph","B"},
    {"Which SQL command retrieves rows?","GET","SELECT","FETCH TABLE","OPEN","B"},{"Which key uniquely identifies each row?","Foreign key","Primary key","Candidate value","Index only","B"},{"Which clause filters grouped records?","WHERE","ORDER BY","HAVING","DISTINCT","C"},{"Which command removes a table definition?","DELETE","REMOVE","DROP","CLEAR","C"},{"A foreign key is used to:","Encrypt data","Link related tables","Sort rows","Create a database","B"},
    {"Which HTML element creates a hyperlink?","<link>","<a>","<href>","<url>","B"},{"Which CSS property changes text color?","font-style","background","color","text-size","C"},{"Which language adds interactivity to web pages?","SQL","JavaScript","XML","C","B"},{"Which HTML attribute provides alternative image text?","title","src","alt","href","C"},{"Which CSS layout system is one-dimensional?","Grid","Flexbox","Float","Table","B"},
    {"Which protocol is used to transfer web pages securely?","HTTP","FTP","HTTPS","SMTP","C"},{"How many layers are in the OSI model?","4","5","7","8","C"},{"Which device forwards packets between networks?","Switch","Router","Repeater","Hub","B"},{"What does DNS translate?","IP to MAC only","Domain names to IP addresses","Files to packets","HTTP to HTTPS","B"},{"Which transport protocol is connection-oriented?","UDP","IP","TCP","ARP","C"}};
   for(int exam=0;exam<10;exam++){Long examId=db.queryForObject("SELECT id FROM exams WHERE title=?",Long.class,tests[exam][0]);Integer existing=db.queryForObject("SELECT COUNT(*) FROM questions WHERE exam_id=?",Integer.class,examId);for(int j=existing;j<10;j++){String[] v=q[(exam*5+j)%q.length];db.update("INSERT INTO questions(exam_id,question_text,option_a,option_b,option_c,option_d,correct_option) VALUES(?,?,?,?,?,?,?)",examId,v[0],v[1],v[2],v[3],v[4],v[5]);}}
  }
  if(db.queryForObject("SELECT COUNT(*) FROM users WHERE email='admin@examvault.com'",Integer.class)==0)db.update("INSERT INTO users(name,email,password_hash,role) VALUES(?,?,?,?)","ExamVault Admin","admin@examvault.com",encoder.encode("Admin@123"),"ADMIN");
 };}
}
@RestController @RequestMapping("/api")
class ExamApi {
 private final JdbcTemplate db;private final PasswordEncoder encoder;
 ExamApi(JdbcTemplate db,PasswordEncoder encoder){this.db=db;this.encoder=encoder;}
 private Long uid(HttpSession s){Object id=s.getAttribute("uid");return id==null?null:(Long)id;}
 private boolean admin(HttpSession s){return "ADMIN".equals(s.getAttribute("role"));}
 private Map<String,Object> user(HttpSession s){Map<String,Object> m=new HashMap<>();m.put("id",uid(s));m.put("name",s.getAttribute("name"));m.put("email",s.getAttribute("email"));m.put("role",s.getAttribute("role"));return m;}
 private ResponseEntity<?> error(String m,HttpStatus h){return ResponseEntity.status(h).body(Map.of("error",m));}
 @PostMapping("/auth/signup") ResponseEntity<?> signup(@RequestBody Map<String,String>b,HttpSession s){
  String name=b.getOrDefault("name","").trim(),email=b.getOrDefault("email","").trim().toLowerCase(),phone=b.getOrDefault("phone","").trim(),pass=b.getOrDefault("password",""); int age; try{age=Integer.parseInt(b.getOrDefault("age","0"));}catch(Exception ex){age=0;}
  if(name.isBlank()||!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")||!phone.matches("^[0-9+() -]{7,20}$")||age<13||age>100||pass.length()<8)return error("Enter valid name, email, phone, age and password.",HttpStatus.BAD_REQUEST);
  if(!db.queryForList("SELECT id FROM users WHERE email=?",email).isEmpty())return error("An account with this email already exists.",HttpStatus.CONFLICT);
  db.update("INSERT INTO users(name,email,phone,age,password_hash,role) VALUES(?,?,?,?,?,'STUDENT')",name,email,phone,age,encoder.encode(pass));return login(Map.of("email",email,"password",pass),s);
 }
 @PostMapping("/auth/login") ResponseEntity<?> login(@RequestBody Map<String,String>b,HttpSession s){
  var rows=db.queryForList("SELECT * FROM users WHERE email=?",b.getOrDefault("email","").trim().toLowerCase());
  if(rows.isEmpty()||!encoder.matches(b.getOrDefault("password",""),(String)rows.get(0).get("PASSWORD_HASH")))return error("Incorrect email or password.",HttpStatus.UNAUTHORIZED); if("ADMIN".equals(b.getOrDefault("portal","STUDENT"))&&!"ADMIN".equals(rows.get(0).get("ROLE")))return error("This account is not an administrator account.",HttpStatus.FORBIDDEN); if("STUDENT".equals(b.getOrDefault("portal","STUDENT"))&&!"STUDENT".equals(rows.get(0).get("ROLE")))return error("Please use administrator sign-in.",HttpStatus.FORBIDDEN);
  var u=rows.get(0);s.setAttribute("uid",((Number)u.get("ID")).longValue());s.setAttribute("name",u.get("NAME"));s.setAttribute("email",u.get("EMAIL"));s.setAttribute("role",u.get("ROLE"));return ResponseEntity.ok(user(s));
 }
 @PostMapping("/auth/logout") Map<String,String> logout(HttpSession s){s.invalidate();return Map.of("message","Signed out");}
 @GetMapping("/health") Map<String,String> health(){return Map.of("status","ok","service","ExamVault API");}
 @GetMapping("/auth/me") ResponseEntity<?> me(HttpSession s){return uid(s)==null?error("Please sign in.",HttpStatus.UNAUTHORIZED):ResponseEntity.ok(user(s));}
 @GetMapping("/exams") ResponseEntity<?> exams(HttpSession s){if(uid(s)==null)return error("Please sign in.",HttpStatus.UNAUTHORIZED);return ResponseEntity.ok(db.queryForList("SELECT e.id,e.title,e.category,e.description,e.duration_minutes,COUNT(q.id) question_count FROM exams e LEFT JOIN questions q ON q.exam_id=e.id WHERE e.active=TRUE GROUP BY e.id,e.title,e.category,e.description,e.duration_minutes ORDER BY e.id"));}
 @GetMapping("/exams/{id}/questions") ResponseEntity<?> questions(@PathVariable long id,HttpSession s){
  if(uid(s)==null)return error("Please sign in.",HttpStatus.UNAUTHORIZED);var exam=db.queryForList("SELECT id,title,duration_minutes FROM exams WHERE id=? AND active=TRUE",id);if(exam.isEmpty())return error("Test not found.",HttpStatus.NOT_FOUND);
  var qs=db.queryForList("SELECT id,question_text,option_a,option_b,option_c,option_d FROM questions WHERE exam_id=? ORDER BY id",id);return ResponseEntity.ok(Map.of("exam",exam.get(0),"questions",qs));
 }
 @PostMapping("/attempts") ResponseEntity<?> submit(@RequestBody Map<String,Object>b,HttpSession s){
  Long userId=uid(s);if(userId==null)return error("Please sign in.",HttpStatus.UNAUTHORIZED);long examId=((Number)b.get("examId")).longValue();var answers=(Map<String,String>)b.getOrDefault("answers",Map.of());var qs=db.queryForList("SELECT id,correct_option FROM questions WHERE exam_id=?",examId);if(qs.isEmpty())return error("Test has no questions.",HttpStatus.NOT_FOUND);
  int score=0;for(var q:qs)if(Objects.equals(answers.get(String.valueOf(q.get("ID"))),q.get("CORRECT_OPTION")))score++;int total=qs.size();double pct=Math.round(score*10000.0/total)/100.0;
  db.update("INSERT INTO attempts(user_id,exam_id,score,total,percentage) VALUES(?,?,?,?,?)",userId,examId,score,total,pct);return ResponseEntity.ok(Map.of("score",score,"total",total,"percentage",pct,"message",pct>=40?"Well done! Keep it up.":"Keep practising. You can improve!"));
 }
 @GetMapping("/results") ResponseEntity<?> results(HttpSession s){if(uid(s)==null)return error("Please sign in.",HttpStatus.UNAUTHORIZED);return ResponseEntity.ok(db.queryForList("SELECT a.id,e.title,e.category,a.score,a.total,a.percentage,a.submitted_at FROM attempts a JOIN exams e ON e.id=a.exam_id WHERE a.user_id=? ORDER BY a.submitted_at DESC",uid(s)));}
 @PostMapping("/admin/questions") ResponseEntity<?> addQuestion(@RequestBody Map<String,String>b,HttpSession s){
  if(uid(s)==null)return error("Please sign in.",HttpStatus.UNAUTHORIZED);if(!admin(s))return error("Administrator access required.",HttpStatus.FORBIDDEN);
  try{long examId=Long.parseLong(b.get("examId"));String q=b.getOrDefault("question","").trim(),a=b.getOrDefault("a","").trim(),bb=b.getOrDefault("b","").trim(),c=b.getOrDefault("c","").trim(),d=b.getOrDefault("d","").trim(),correct=b.getOrDefault("correct","");
   if(q.isBlank()||a.isBlank()||bb.isBlank()||c.isBlank()||d.isBlank()||!List.of("A","B","C","D").contains(correct))return error("Complete every field and choose the correct option.",HttpStatus.BAD_REQUEST);
   db.update("INSERT INTO questions(exam_id,question_text,option_a,option_b,option_c,option_d,correct_option,explanation) VALUES(?,?,?,?,?,?,?,?)",examId,q,a,bb,c,d,correct,b.getOrDefault("explanation",""));return ResponseEntity.ok(Map.of("message","Question added successfully."));
  }catch(Exception ex){return error("Could not add question. Check the selected test.",HttpStatus.BAD_REQUEST);}
 }
}