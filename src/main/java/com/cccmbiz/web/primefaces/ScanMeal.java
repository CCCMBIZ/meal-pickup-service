package com.cccmbiz.web.primefaces;

import com.cccmbiz.web.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.annotation.RequestScope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.*;

@Named
@RequestScope
public class ScanMeal {

    private static final Logger logger = LoggerFactory.getLogger(ScanMeal.class);

    @Autowired
    // Which is auto-generated by Spring, we will use it to handle the data
    private PersonRepository personRepository;

    @Autowired
    // Which is auto-generated by Spring, we will use it to handle the data
    private MealplanRepository mealplanRepository;

    @Autowired
    // Which is auto-generated by Spring, we will use it to handle the data
    private MealtrackerRepository mealtrackerRepository;

    private List mealtrackers;
    private String query;

    private String mealTotalCount;
    private String mealTakenCount;
    private String mealLeftCount;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List getMealtrackers() {
        return this.mealtrackers;
    }

    public String enter() throws ScanMealException {

        reset();

        if (query != null && !query.isEmpty()) {

            FacesContext context = FacesContext.getCurrentInstance();

            try {

                Integer searchNmbr = Integer.parseInt(query);

                Optional<Person> op = personRepository.findById(searchNmbr);
                Person person = op.get();

                long firstDayDinner = new GregorianCalendar(2018, Calendar.DECEMBER, 12, 16, 00).getTimeInMillis();
                long secondDayBreakfast = new GregorianCalendar(2018, Calendar.DECEMBER, 29, 4, 0).getTimeInMillis();
                long secondDayLunch = new GregorianCalendar(2018, Calendar.DECEMBER, 29, 10, 0).getTimeInMillis();
                long secondDayDinner = new GregorianCalendar(2018, Calendar.DECEMBER, 29, 16, 0).getTimeInMillis();
                long thirdDayBreakfast = new GregorianCalendar(2018, Calendar.DECEMBER, 30, 4, 0).getTimeInMillis();
                long thirdDayLunch = new GregorianCalendar(2018, Calendar.DECEMBER, 30, 10, 0).getTimeInMillis();
                long thirdDayDinner = new GregorianCalendar(2018, Calendar.DECEMBER, 30, 16, 0).getTimeInMillis();
                long fourthDayBreakfast = new GregorianCalendar(2018, Calendar.DECEMBER, 31, 4, 0).getTimeInMillis();
                long fourthDayLunch = new GregorianCalendar(2018, Calendar.DECEMBER, 31, 10, 0).getTimeInMillis();

                // Obtain current time
                DateTime now = DateTime.now();
                Timestamp ts = new Timestamp(now.getMillis());

                // Start meal tracking
                Mealtracker mt = new Mealtracker();
                mt.setPersonId(person.getPersonId());
                mt.setRegistrationId(String.valueOf(person.getFamilyId()));
                mt.setLastModified(ts);

                // Construct display name
                String name = null;

                if (person.getChineseName() != null && !person.getChineseName().isEmpty()) {
                    name = person.getChineseName();
                }
                if (person.getFirstName() != null && !person.getFirstName().isEmpty()) {
                    if (name == null) {
                        name = person.getFirstName();

                    } else {
                        name += " ";
                        name += person.getFirstName();
                    }
                }
                if (person.getLastName() != null && !person.getLastName().isEmpty()) {
                    if (name == null) {
                        name = person.getLastName();

                    } else {
                        name += " ";
                        name += person.getLastName();
                    }
                }

                logger.debug("name:" + name);
                mt.setRemark(name);
                logger.debug("remark:" + mt.getRemark());

                // Find the meal plan record of current scanned registrant
                Optional<Mealplan> omp = mealplanRepository.findById(mt.getRegistrationId());
                Mealplan mp = omp.get();

                String caption = "";
                Integer mealTotal = 0;

                if (now.isAfter(fourthDayLunch)) {
                    caption = "LUNCH4";
                    mealTotal = mp.getLunch4();
                } else if (now.isAfter((fourthDayBreakfast))) {
                    caption = "BREAKFAST4";
                    mealTotal = mp.getBreakfast4();
                } else if (now.isAfter((thirdDayDinner))) {
                    caption = "DINNER3";
                    mealTotal = mp.getDinner3();
                } else if (now.isAfter((thirdDayLunch))) {
                    caption = "LUNCH3";
                    mealTotal = mp.getLunch3();
                } else if (now.isAfter((thirdDayBreakfast))) {
                    caption = "BREAKFAST3";
                    mealTotal = mp.getBreakfast3();
                } else if (now.isAfter((secondDayDinner))) {
                    caption = "DINNER2";
                    mealTotal = mp.getDinner2();
                } else if (now.isAfter((secondDayLunch))) {
                    caption = "LUNCH2";
                    mealTotal = mp.getLunch2();
                } else if (now.isAfter((secondDayBreakfast))) {
                    caption = "BREAKFAST2";
                    mealTotal = mp.getBreakfast2();
                } else if (now.isAfter((firstDayDinner))) {
                    caption = "DINNER1";
                    mealTotal = mp.getDinner1();
                } else {
                }

                mt.setMealType(caption);

                mealtrackers = mealtrackerRepository.findByRegistrationIdAndMealType(mt.getRegistrationId(), caption);

                // Obtain taken record of current scanned registrant
                Integer taken = 0;
                if (mealtrackers != null && !mealtrackers.isEmpty()) {
                    taken = mealtrackers.size();
                } else {
                    // Empty list
                    mealtrackers = new ArrayList<>();
                }

                if (mealTotal == 0) {
                    logger.info("No Order Record");

                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "抱歉! ", "沒有订餐记录。"));
                } else if (mealTotal <= taken) {
                    logger.info("Exceed Order Count");

                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "抱歉! ", "己領了全部的饭盒。"));
                } else {
                    mealtrackerRepository.save(mt);
                    taken++;
                    mealtrackers.add(mt);
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "成动! ", "请拿饭盒。"));

                }

                mealTotalCount = new StringBuilder()
                        .append("订了 ")
                        .append(mealTotal)
                        .toString();

                mealTakenCount = new StringBuilder()
                        .append("領了 ")
                        .append(taken)
                        .toString();

                StringBuilder sb = new StringBuilder();
                sb.append("剩下  ");
                sb.append(mealTotal - taken);
                mealLeftCount = sb.toString();


                Collections.sort(mealtrackers, (Comparator<Mealtracker>) (m1, m2) -> m2.getLastModified().compareTo(m1.getLastModified()));


            } catch (NoSuchElementException noSuchElementException) {
                logger.error("Scan Error:" + noSuchElementException.getMessage());
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "抱歉! ", "没有这个注册记录"));
            } catch (NumberFormatException numberFormatException) {
                logger.error("Scan Error:" + numberFormatException.getMessage());
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "抱歉! ", "請輸入正確注册号码"));
            } finally {
                query = "";
            }
        }

        return "success";
    }


    private void reset() {
        if (mealtrackers != null) {
            mealtrackers.clear();
        }
        mealTotalCount = "";
        mealTakenCount = "";
        mealLeftCount = "";
    }

    /**
     * @return the mealTotalCount
     */
    public String getMealTotalCount() {
        return mealTotalCount;
    }

    /**
     * @return the mealTakenCount
     */
    public String getMealTakenCount() {
        return mealTakenCount;
    }

    /**
     * @return the mealLeftCount
     */
    public String getMealLeftCount() {
        return mealLeftCount;
    }

}