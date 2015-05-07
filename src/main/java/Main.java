import static spark.Spark.*;

import java.net.URL;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import org.mindrot.jbcrypt.BCrypt;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import spark.ModelAndView;
import spark.Request;
import spark.template.freemarker.FreeMarkerEngine;

public class Main {
	public static void main(String args[]){
		
		DataBase.connect("jdbc:mysql://localhost/BorrowBit", "LIRData", "LIRDATA_69");
		staticFileLocation("/public");
		port(9090);
		
		Account a = new Account();
		
		FreeMarkerEngine engine = new FreeMarkerEngine();
		
		Configuration c = new Configuration();
		
		//ClassLoader.getSystemResource("/");
		URL url = ClassLoader.getSystemResource("/");
		
		c.setClassLoaderForTemplateLoading(ClassLoader.getSystemClassLoader(), "/resources/spark/template/freemarker/");
		//TODO Don't fix <---------
		try{
			c.getTemplate("index.html");
		}catch(Exception e){
			c.setClassLoaderForTemplateLoading(ClassLoader.getSystemClassLoader(), "/spark/template/freemarker/");
		}
		
		//c.setTemplateLoader(ClassLoader.getSystemClassLoader(), "/resources/spark/template/freemarker/");
		
		engine.setConfiguration(c);
		
		get("/hello", (req, res) -> {
			return "Sweg";
		});
		get("/form", (req, res) -> {
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			attributes.put("name", "Alex Banks");
			return new ModelAndView(attributes, "template.txt");
		}, engine);
		get("/formsubmit.html", (req, res) -> {
			String name = req.queryParams("username");
			String password = req.queryParams("psw");
			String response = "<p>Hello " + name + " your password is " +
			password + ".</p>";
			return response;
		});
		get("login.html", (req, res) -> {
			
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			
			return new ModelAndView(attributes, "login.html");
		}, engine);
		get("make.html", (req, res) -> {
			
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			
			return new ModelAndView(attributes, "make.html");
		}, engine);
		get("maked.html", (req, res) -> {
			
			Account account = new Account();
			
			int result = account.Session(getID(req));
			
			String message = new String();
			
			String ref = new String();
			
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			
			if(result == Account.S_SUCCESS){
			
				String desc = req.queryParams("desc");
				
				String item = req.queryParams("item");
				
				PreparedStatement statement = DataBase.getUpdate("INSERT INTO Request (name, email, item, note, created) VALUES (?, ?, ?, ?, CURDATE());");
				
				try{
					statement.setString(1, account.name);
					statement.setString(2, account.email);
					statement.setString(3, item);
					statement.setString(4, desc);
				}catch(Exception e){
					
				}
				
				DataBase.sendUpdate(statement);
				
				message = "Successfuly made request";
				ref = "index.html";
			
			}else{
				
				message = "You must be signed in to do this!";
				ref = "/index.html";
				
			}
			
			attributes.put("message", message);
			
			attributes.put("ref", ref);
			
			return new ModelAndView(attributes, "error.html");
		}, engine);
		get("view.html", (req, res) -> {
			
			Account account = new Account();
			
			int result = account.Session(getID(req));
			
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			
			if(result == Account.S_NOT){
				
				attributes.put("message", "You must be signed in to view this!");
				attributes.put("ref", "/index.html");
				
				return new ModelAndView(attributes, "error.html");
			}
			
			int index = Integer.parseInt(req.queryParams("index"));
			
			PreparedStatement statement = DataBase.getQuerry("SELECT * FROM Request WHERE id=?");
			try {
				statement.setInt(1, index);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			ResultSet set = DataBase.sendQuery(statement);
			
			String name = new String();
			String email = new String();
			String item = new String();
			String note = new String();
			Date date = new Date(0);
			
			try {
				set.first();
				
				name = set.getString("name");
				
				email = set.getString("email");
				
				item = set.getString("item");
				
				note = set.getString("note");
				
				date = set.getDate("created");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			attributes.put("Data", "On " + date.toString() + " " + name + " Requested a(n) " + item);
			
			attributes.put("Info", note);
			
			attributes.put("Email", email);
			
			return new ModelAndView(attributes, "view.html");
		}, engine);
		try {
		get("index.html", (req, res) -> {
			Account account = new Account();
			
			int result = account.Session(getID(req));
			
			String userName = new String();
			
			switch(result){
			case Account.S_NOT:
				userName = "Guest";
				break;
			case Account.S_SUCCESS:
				userName = account.name;
				break;
			}
			int perpage = 10;
			int page = 0;
			int maxIndex = 0;
			PreparedStatement statement = DataBase.getQuerry("SELECT COUNT(*) FROM Request");
			ResultSet set = DataBase.sendQuery(statement);
			try {
				set.first();
				maxIndex = set.getInt("COUNT(*)");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			try{
				page = Integer.parseInt(req.queryParams("page"));
			}catch(Exception e){
				page = maxIndex/perpage;
			}
			int offset = page*perpage;
			statement = DataBase.getQuerry("SELECT * FROM Request LIMIT ? OFFSET ?");
			try {
				statement.setInt(1, perpage);
				statement.setInt(2, offset);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			set = DataBase.sendQuery(statement);
			
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			
			String table = new String();
			
			try{
				while(set.next()){
					table += "<tr><td><p>";
					table += set.getString("name");
					table += "</p></td><td><p>";
					table += set.getString("item");
					table += "</p></td><td><a href=\"view.html?index="+ set.getInt("id") + "\">Respond</a></td></tr>";
				}
			}catch(Exception e){
			}
			
			String nav = new String();
			
			nav += "<p>Page " + (page+1) + "</p>";
			
			if(page > 0){
				nav += "<button onclick=\"location.href='/index.html?page=" + (page-1) + "'\">< Last</button>";
			}
			
			if(page < (maxIndex/perpage)){
				nav += "<button onclick=\"location.href='/index.html?page=" + (page+1) + "'\">Next ></button>";
			}
			
			attributes.put("nav", nav);
			
			attributes.put("table", table);
			
			attributes.put("UserName", userName);
			
			return new ModelAndView(attributes, "index.html");
		}, engine);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Uber kewl");
		}
		get("logoutd.html", (req, res) -> {
			
			Account account = new Account();
			
			account.Session(getID(req));
			
			int result = account.logOut();
			
			String message = new String();
			String ref = new String();
			
			switch(result){
			
			case Account.O_NOT:
				message = "Not signed in!";
				break;
			case Account.O_SUCCESS:
				message = "Signed out!";
			break;
			}
			ref = "/index.html";
			
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			
			attributes.put("message", message);
			attributes.put("ref", ref);
			
			return new ModelAndView(attributes, "error.html");
		}, engine);
		get("logind.html", (req, res) -> {
			
			Account account = new Account();
			
			String username = req.queryParams("username");
			String password = req.queryParams("password");
			
			int result = account.Login(username, password, getID(req));
			
			String message = new String();
			String ref = new String();
			boolean valid = true;
			
			switch(result){
			
			case Account.L_ALREADY:
				message = "Already signed in!";
				valid = true;
				break;
			case Account.L_SUCCESS:
				message = "Signed in!";
				valid = true;
			break;
			case Account.L_INVALID:
				message = "Invalid login!";
				valid = false;
			break;
			
			}
			if(valid){
				ref = "/index.html";
			}else{
				ref = "/login.html";
			}
			
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			
			attributes.put("message", message);
			attributes.put("ref", ref);
			
			return new ModelAndView(attributes, "error.html");
		}, engine);
		get("created.html", (req, res) -> {
			
			Account account = new Account();
			
			String username = req.queryParams("username");
			String password = req.queryParams("password");
			String email = req.queryParams("email");
			
			int result = account.Register(username, email, password, getID(req));
			
			String message = new String();
			String ref = new String();
			boolean valid = true;
			
			switch(result){
			
			case Account.R_N_SHORT:
				message = "Username must be 4 chars long!";
				valid = false;
				break;
			case Account.R_N_LONG:
				message = "Username too long!";
				valid = false;
			break;
			
			case Account.R_P_SHORT:
				message = "Password must be 7 chars long!";
				valid = false;
			break;
			case Account.R_P_LONG:
				message = "Password too long!";
				valid = false;
			break;
			
			case Account.R_E_SHORT:
				message = "Email too short!";
				valid = false;
			break;
			
			case Account.R_E_LONG:
				message = "Email too long!";
				valid = false;
			break;
			case Account.R_E_INVALID:
				message = "Email is invalid!";
				valid = false;
			break;
			case Account.R_ALREADY:
				message = "Already signed in";
				valid = true;
			break;
			case Account.R_EXISTS:
				message = "Account Already Exists";
				valid = false;
			break;
			case Account.R_SUCCESS:
				message = "Account created!";
				valid = true;
			break;
			}
			if(valid){
				ref = "/index.html";
			}else{
				ref = "/create.html";
			}
			
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			
			attributes.put("message", message);
			attributes.put("ref", ref);
			
			return new ModelAndView(attributes, "error.html");
		}, engine);
		get("create.html", (req, res) -> {
			
			HashMap<String, Object> attributes = new HashMap<String, Object>();
			
			return new ModelAndView(attributes, "create.html");
		}, engine);
	}
	public static String getID(Request r){
		return r.session().id();
	}
}
