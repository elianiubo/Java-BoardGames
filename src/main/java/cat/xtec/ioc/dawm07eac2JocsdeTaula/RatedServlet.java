package cat.xtec.ioc.dawm07eac2JocsdeTaula;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Joel Monné Mesalles
 */
@WebServlet(name = "Valorat", urlPatterns = {"/valorat"})
public class RatedServlet extends HttpServlet {

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
        if (action == null) {

        } else {
            switch (action) {
                case "llistaJocsValorats":
                    llistaJocsValorats(request, response);
                    break;
                case "eliminarValoracioJoc":
                    eliminarValoracioJoc(request, response);
                    break;
            }
        }
    }
    private void llistaJocsValorats(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setCharacterEncoding("utf-8");
            PrintWriter out = response.getWriter();
            JSONObject json = new JSONObject();
            JSONArray array = new JSONArray();
            ValoracioLocal ratebean = (ValoracioLocal) request.getSession().getAttribute("ratebean");
            if (ratebean == null) {
                ratebean = (ValoracioLocal) new InitialContext().lookup("java:global/dawm07eac2JocsdeTaula/Valoracio");
                ratebean.setJocsValorats(new ArrayList<Joc>());
                request.getSession().setAttribute("ratebean", ratebean);
                //TODO LLISTA BUIDA
            }
            response.setContentType("application/json");
            for (Joc joc : ratebean.getJocsValorats()) {
                LinkedHashMap<String, String> jsonOrderedMap = new LinkedHashMap<String, String>();
                jsonOrderedMap.put("name", joc.getName());
                jsonOrderedMap.put("valoracio", joc.getValoracio().toString());
                jsonOrderedMap.put("mitjana", joc.getMitjana().toString());
                jsonOrderedMap.put("llistaValoracions", (new JSONArray(joc.getTotesValoracions())).toString());
                JSONObject member = new JSONObject(jsonOrderedMap);
                array.put(member);
            }
            json.put("jsonArray", array);
            out.print(json.toString());
            out.close();            
        }catch (Exception ex) {
            System.out.println("error message: " + ex.getMessage());

        }
    }
    
    private void eliminarValoracioJoc(HttpServletRequest request, HttpServletResponse response) {
        try {
            String jocAEliminar = request.getParameter("joc");
            ValoracioLocal ratebean = (ValoracioLocal) request.getSession().getAttribute("ratebean");

            for (Joc joc : ratebean.getJocsValorats()) {
                if (joc.getName().equals(jocAEliminar)) {
                    joc.eliminaUltimaValoracio();
                    ratebean.getJocsValorats().remove(joc);
                    break;
                }
            }
            String toReturn = "OK";
            PrintWriter out = response.getWriter();
            response.setCharacterEncoding("utf-8");
            JSONObject json = new JSONObject();
            json.put("resposta", toReturn);
            out.print(json.toString());
            out.close();
        } catch (Exception ex) {
            System.out.println("error message: " + ex.getMessage());

        }
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
