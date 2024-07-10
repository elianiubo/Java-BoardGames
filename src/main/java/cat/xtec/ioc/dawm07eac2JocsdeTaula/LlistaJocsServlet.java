package cat.xtec.ioc.dawm07eac2JocsdeTaula;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Joel Monné Mesalles
 */
@MultipartConfig(location = "/tmp")
public class LlistaJocsServlet extends HttpServlet {

    @EJB
    private ValidateJocBeanLocal validation;
        
    private List<Joc> jocs = new ArrayList<Joc>();
    
    //Directori on es guarden les imatges
    private static final String UPLOAD_DIR = "img";
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Enumeration initValorations= getServletConfig().getInitParameterNames();
        while (initValorations.hasMoreElements()) {
            String name = (String) initValorations.nextElement();
            String values = getServletConfig().getInitParameter(name);
            Integer valoracio = Integer.parseInt(values);
            jocs.add(new Joc(name, valoracio));
            
        }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        switch (action) {
            case "llistaJocs":
                llistaJocs(request, response);
                break;
            case "afegirValoracioJoc":
                afegirValoracioJoc(request, response);
                break;
            case "afegirNouJoc":
                afegirNouJoc(request, response);
                break;
        }
    }

    
    protected void llistaJocs(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        try (PrintWriter out = response.getWriter()) {
            for (Joc joc : jocs) {
                response.setContentType("application/json");
                LinkedHashMap<String, String> jsonOrderedMap = new LinkedHashMap<String, String>();
                jsonOrderedMap.put("name", joc.getName());
                jsonOrderedMap.put("valoracio", joc.getValoracio().toString());
                jsonOrderedMap.put("mitjana", joc.getMitjana().toString());
                /*
                P3 Si ja hi és al llistat de valorats
                 */
                if (comprovarJocValoratSession(request, joc)) {
                    jsonOrderedMap.put("valorat", "SI");
                } else {
                    jsonOrderedMap.put("valorat", "NO");
                }

                /*
                END P3
                 */
                JSONObject member = new JSONObject(jsonOrderedMap);
                array.put(member);
            }
            json.put("jsonArray", array);
            out.print(json.toString());
            out.close();
        } catch (JSONException ex) {
            Logger.getLogger(LlistaJocsServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void afegirValoracioJoc(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String jocValorat= request.getParameter("joc");
        Integer valoracio = Integer.parseInt(request.getParameter("valoracio"));
        try (PrintWriter out = response.getWriter()) {
            JSONObject json = new JSONObject();
            Double mitjana = -1.0;
            for (Joc joc : this.jocs) {
                if (joc.getName().equals(jocValorat)) {
                    joc.setValoracio(valoracio);
                    mitjana = joc.getMitjana();
                    /*
                     P3
                     */
                    afegirJocValoratToSession(request, joc);
                    break;
                }
            }
            json.put("jocValorat", jocValorat);
            json.put("mitjanaJoc", mitjana);
            out.print(json.toString());
            out.close();

        } catch (JSONException ex) {
            Logger.getLogger(LlistaJocsServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    

    protected void afegirNouJoc(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // gets absolute path of the web application
        String applicationPath = request.getServletContext().getRealPath("");
        // constructs path of the directory to save uploaded file
        String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;
        try {
            File fileSaveDir = new File(uploadFilePath);
            System.out.println("Upload File Directory=" + fileSaveDir.getAbsolutePath());
            Part p = request.getPart("fileImageName");
            if (isValidFileName(request.getParameter("fpname"), p.getSubmittedFileName())) {
                File file = new File(uploadFilePath, p.getSubmittedFileName());
                InputStream input = p.getInputStream();
                Files.copy(input, file.toPath(), REPLACE_EXISTING);
                Joc jocNou= new Joc(request.getParameter("fpname"), Integer.parseInt(request.getParameter("fprate")));
                this.jocs.add(jocNou);
                response.sendRedirect("index.html");
            } else {
                System.out.println("getRequestDispatcher CONTEXT BEFORE");
                response.sendRedirect("index.html");
                response.setHeader("errorname", "Joc no creat per nom incorrecte");
                System.out.println("getRequestDispatcher CONTEXT AFTER");
            }

        } catch (Exception e) {
            System.out.println("error message: " + e.getMessage());
        }

    }
    
    private boolean isValidFileName(String paramName, String fileName){
        boolean valid = false;
        valid = validation.isValidFileImageName(paramName, fileName);
        for (Joc joc: jocs) {
            if (joc.getName().equals(paramName)){
                valid = false;
            }
        }
        return valid;       
    }
    
    /*
     P3
     */
    private void afegirJocValoratToSession(HttpServletRequest request, Joc joc) {
        /*
         P3
         */
        try {
            ValoracioLocal ratebean = (ValoracioLocal) request.getSession().getAttribute("ratebean");
            if (ratebean == null) {
                ratebean = (ValoracioLocal) new InitialContext().lookup("java:global/dawm07eac2JocsdeTaula/Valoracio");
                ratebean.setJocsValorats(new ArrayList<Joc>());
                request.getSession().setAttribute("ratebean", ratebean);
            }
            ratebean.getJocsValorats().add(joc);
        } catch (Exception ex) {
            System.out.println("error message: " + ex.getMessage());

        }
    }
    
    
    private Boolean comprovarJocValoratSession(HttpServletRequest request, Joc joc) {
        Boolean toReturn = false;
        try {
            
            ValoracioLocal ratebean = (ValoracioLocal) request.getSession().getAttribute("ratebean");
            if (ratebean == null) {
                ratebean = (ValoracioLocal) new InitialContext().lookup("java:global/dawm07eac2JocsdeTaula/Valoracio");
                ratebean.setJocsValorats(new ArrayList<Joc>());
                request.getSession().setAttribute("cartbean", ratebean);
            }            
            for (Joc p : ratebean.getJocsValorats()) {
                if(p.getName().equals(joc.getName())){
                    toReturn = true;
                    break;
                }                
            }           
        } catch (Exception ex) {
            System.out.println("error message: " + ex.getMessage());

        }
        return toReturn;
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
