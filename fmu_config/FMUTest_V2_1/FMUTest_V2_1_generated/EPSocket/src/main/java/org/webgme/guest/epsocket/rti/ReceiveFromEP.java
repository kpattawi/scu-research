package org.webgme.guest.epsocket.rti;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cpswt.utils.CpswtUtils;

import hla.rti.FederateNotExecutionMember;
import hla.rti.InteractionClassNotDefined;
import hla.rti.InteractionClassNotPublished;
import hla.rti.InteractionClassNotSubscribed;
import hla.rti.LogicalTime;
import hla.rti.NameNotFound;
import hla.rti.RTIambassador;
import hla.rti.ReceivedInteraction;

import org.cpswt.hla.*;

/**
* Implements InteractionRoot.C2WInteractionRoot.ReceiveFromEP
*/
public class ReceiveFromEP extends C2WInteractionRoot {

    private static final Logger logger = LogManager.getLogger();

    /**
    * Creates an instance of the ReceiveFromEP interaction class with default parameter values.
    */
    public ReceiveFromEP() {}

    private static int _Data_handle;
    private static int _NumberOfVariable_handle;
    private static int _SimID_handle;
    private static int _actualLogicalGenerationTime_handle;
    private static int _federateFilter_handle;
    private static int _originFed_handle;
    private static int _sourceFed_handle;

    private static boolean _isInitialized = false;

    private static int _handle;

    /**
    * Returns the handle (RTI assigned) of the ReceiveFromEP interaction class.
    * Note: As this is a static method, it is NOT polymorphic, and so, if called on
    * a reference will return the handle of the class pertaining to the reference,
    * rather than the handle of the class for the instance referred to by the reference.
    * For the polymorphic version of this method, use {@link #getClassHandle()}.
    *
    * @return the RTI assigned integer handle that represents this interaction class
    */
    public static int get_handle() {
        return _handle;
    }

    /**
    * Returns the fully-qualified (dot-delimited) name of the ReceiveFromEP interaction class.
    * Note: As this is a static method, it is NOT polymorphic, and so, if called on
    * a reference will return the name of the class pertaining to the reference,
    * rather than the name of the class for the instance referred to by the reference.
    * For the polymorphic version of this method, use {@link #getClassName()}.
    *
    * @return the fully-qualified HLA class path for this interaction class
    */
    public static String get_class_name() {
        return "InteractionRoot.C2WInteractionRoot.ReceiveFromEP";
    }

    /**
    * Returns the simple name (the last name in the dot-delimited fully-qualified
    * class name) of the ReceiveFromEP interaction class.
    *
    * @return the name of this interaction class
    */
    public static String get_simple_class_name() {
        return "ReceiveFromEP";
    }

    private static Set< String > _datamemberNames = new HashSet< String >();
    private static Set< String > _allDatamemberNames = new HashSet< String >();

    /**
    * Returns a set containing the names of all of the non-hidden parameters in the
    * ReceiveFromEP interaction class.
    * Note: As this is a static method, it is NOT polymorphic, and so, if called on
    * a reference will return a set of parameter names pertaining to the reference,
    * rather than the parameter names of the class for the instance referred to by
    * the reference.  For the polymorphic version of this method, use
    * {@link #getParameterNames()}.
    *
    * @return a modifiable set of the non-hidden parameter names for this interaction class
    */
    public static Set< String > get_parameter_names() {
        return new HashSet< String >(_datamemberNames);
    }

    /**
    * Returns a set containing the names of all of the parameters in the
    * ReceiveFromEP interaction class.
    * Note: As this is a static method, it is NOT polymorphic, and so, if called on
    * a reference will return a set of parameter names pertaining to the reference,
    * rather than the parameter names of the class for the instance referred to by
    * the reference.  For the polymorphic version of this method, use
    * {@link #getParameterNames()}.
    *
    * @return a modifiable set of the parameter names for this interaction class
    */
    public static Set< String > get_all_parameter_names() {
        return new HashSet< String >(_allDatamemberNames);
    }

    static {
        _classNameSet.add("InteractionRoot.C2WInteractionRoot.ReceiveFromEP");
        _classNameClassMap.put("InteractionRoot.C2WInteractionRoot.ReceiveFromEP", ReceiveFromEP.class);

        _datamemberClassNameSetMap.put("InteractionRoot.C2WInteractionRoot.ReceiveFromEP", _datamemberNames);
        _allDatamemberClassNameSetMap.put("InteractionRoot.C2WInteractionRoot.ReceiveFromEP", _allDatamemberNames);

        _datamemberNames.add("Data");
        _datamemberNames.add("NumberOfVariable");
        _datamemberNames.add("SimID");

        _datamemberTypeMap.put("Data", "String");
        _datamemberTypeMap.put("NumberOfVariable", "String");
        _datamemberTypeMap.put("SimID", "int");

        _allDatamemberNames.add("Data");
        _allDatamemberNames.add("NumberOfVariable");
        _allDatamemberNames.add("SimID");
        _allDatamemberNames.add("actualLogicalGenerationTime");
        _allDatamemberNames.add("federateFilter");
        _allDatamemberNames.add("originFed");
        _allDatamemberNames.add("sourceFed");
    }

    protected static void init(RTIambassador rti) {
        if (_isInitialized) return;
        _isInitialized = true;

        C2WInteractionRoot.init(rti);

        boolean isNotInitialized = true;
        while(isNotInitialized) {
            try {
                _handle = rti.getInteractionClassHandle("InteractionRoot.C2WInteractionRoot.ReceiveFromEP");
                isNotInitialized = false;
            } catch (FederateNotExecutionMember e) {
                logger.error("could not initialize: Federate Not Execution Member", e);
                return;
            } catch (NameNotFound e) {
                logger.error("could not initialize: Name Not Found", e);
                return;
            } catch (Exception e) {
                logger.error(e);
                CpswtUtils.sleepDefault();
            }
        }

        _classNameHandleMap.put("InteractionRoot.C2WInteractionRoot.ReceiveFromEP", get_handle());
        _classHandleNameMap.put(get_handle(), "InteractionRoot.C2WInteractionRoot.ReceiveFromEP");
        _classHandleSimpleNameMap.put(get_handle(), "ReceiveFromEP");

        isNotInitialized = true;
        while(isNotInitialized) {
            try {
                _Data_handle = rti.getParameterHandle("Data", get_handle());
                _NumberOfVariable_handle = rti.getParameterHandle("NumberOfVariable", get_handle());
                _SimID_handle = rti.getParameterHandle("SimID", get_handle());
                _actualLogicalGenerationTime_handle = rti.getParameterHandle("actualLogicalGenerationTime", get_handle());
                _federateFilter_handle = rti.getParameterHandle("federateFilter", get_handle());
                _originFed_handle = rti.getParameterHandle("originFed", get_handle());
                _sourceFed_handle = rti.getParameterHandle("sourceFed", get_handle());
                isNotInitialized = false;
            } catch (FederateNotExecutionMember e) {
                logger.error("could not initialize: Federate Not Execution Member", e);
                return;
            } catch (InteractionClassNotDefined e) {
                logger.error("could not initialize: Interaction Class Not Defined", e);
                return;
            } catch (NameNotFound e) {
                logger.error("could not initialize: Name Not Found", e);
                return;
            } catch (Exception e) {
                logger.error(e);
                CpswtUtils.sleepDefault();
            }
        }

        _datamemberNameHandleMap.put("InteractionRoot.C2WInteractionRoot.ReceiveFromEP.Data", _Data_handle);
        _datamemberNameHandleMap.put("InteractionRoot.C2WInteractionRoot.ReceiveFromEP.NumberOfVariable", _NumberOfVariable_handle);
        _datamemberNameHandleMap.put("InteractionRoot.C2WInteractionRoot.ReceiveFromEP.SimID", _SimID_handle);
        _datamemberNameHandleMap.put("InteractionRoot.C2WInteractionRoot.ReceiveFromEP.actualLogicalGenerationTime", _actualLogicalGenerationTime_handle);
        _datamemberNameHandleMap.put("InteractionRoot.C2WInteractionRoot.ReceiveFromEP.federateFilter", _federateFilter_handle);
        _datamemberNameHandleMap.put("InteractionRoot.C2WInteractionRoot.ReceiveFromEP.originFed", _originFed_handle);
        _datamemberNameHandleMap.put("InteractionRoot.C2WInteractionRoot.ReceiveFromEP.sourceFed", _sourceFed_handle);

        _datamemberHandleNameMap.put(_Data_handle, "Data");
        _datamemberHandleNameMap.put(_NumberOfVariable_handle, "NumberOfVariable");
        _datamemberHandleNameMap.put(_SimID_handle, "SimID");
        _datamemberHandleNameMap.put(_actualLogicalGenerationTime_handle, "actualLogicalGenerationTime");
        _datamemberHandleNameMap.put(_federateFilter_handle, "federateFilter");
        _datamemberHandleNameMap.put(_originFed_handle, "originFed");
        _datamemberHandleNameMap.put(_sourceFed_handle, "sourceFed");
    }

    private static boolean _isPublished = false;

    /**
    * Publishes the ReceiveFromEP interaction class for a federate.
    *
    * @param rti handle to the Local RTI Component
    */
    public static void publish(RTIambassador rti) {
        if (_isPublished) return;

        init(rti);

        synchronized(rti) {
            boolean isNotPublished = true;
            while(isNotPublished) {
                try {
                    rti.publishInteractionClass(get_handle());
                    isNotPublished = false;
                } catch (FederateNotExecutionMember e) {
                    logger.error("could not publish: Federate Not Execution Member", e);
                    return;
                } catch (InteractionClassNotDefined e) {
                    logger.error("could not publish: Interaction Class Not Defined", e);
                    return;
                } catch (Exception e) {
                    logger.error(e);
                    CpswtUtils.sleepDefault();
                }
            }
        }

        _isPublished = true;
        logger.debug("publish: {}", get_class_name());
    }

    /**
    * Unpublishes the ReceiveFromEP interaction class for a federate.
    *
    * @param rti handle to the Local RTI Component
    */
    public static void unpublish(RTIambassador rti) {
        if (!_isPublished) return;

        init(rti);

        synchronized(rti) {
            boolean isNotUnpublished = true;
            while(isNotUnpublished) {
                try {
                    rti.unpublishInteractionClass(get_handle());
                    isNotUnpublished = false;
                } catch (FederateNotExecutionMember e) {
                    logger.error("could not unpublish: Federate Not Execution Member", e);
                    return;
                } catch (InteractionClassNotDefined e) {
                    logger.error("could not unpublish: Interaction Class Not Defined", e);
                    return;
                } catch (InteractionClassNotPublished e) {
                    logger.error("could not unpublish: Interaction Class Not Published", e);
                    return;
                } catch (Exception e) {
                    logger.error(e);
                    CpswtUtils.sleepDefault();
                }
            }
        }

        _isPublished = false;
        logger.debug("unpublish: {}", get_class_name());
    }

    private static boolean _isSubscribed = false;

    /**
    * Subscribes a federate to the ReceiveFromEP interaction class.
    *
    * @param rti handle to the Local RTI Component
    */
    public static void subscribe(RTIambassador rti) {
        if (_isSubscribed) return;

        init(rti);

        synchronized(rti) {
            boolean isNotSubscribed = true;
            while(isNotSubscribed) {
                try {
                    rti.subscribeInteractionClass(get_handle());
                    isNotSubscribed = false;
                } catch (FederateNotExecutionMember e) {
                    logger.error("could not subscribe: Federate Not Execution Member", e);
                    return;
                } catch (InteractionClassNotDefined e) {
                    logger.error("could not subscribe: Interaction Class Not Defined", e);
                    return;
                } catch (Exception e) {
                    logger.error(e);
                    CpswtUtils.sleepDefault();
                }
            }
        }

        _isSubscribed = true;
        logger.debug("subscribe: {}", get_class_name());
    }

    /**
    * Unsubscribes a federate from the ReceiveFromEP interaction class.
    *
    * @param rti handle to the Local RTI Component
    */
    public static void unsubscribe(RTIambassador rti) {
        if (!_isSubscribed) return;

        init(rti);

        synchronized(rti) {
            boolean isNotUnsubscribed = true;
            while(isNotUnsubscribed) {
                try {
                    rti.unsubscribeInteractionClass(get_handle());
                    isNotUnsubscribed = false;
                } catch (FederateNotExecutionMember e) {
                    logger.error("could not unsubscribe: Federate Not Execution Member", e);
                    return;
                } catch (InteractionClassNotDefined e) {
                    logger.error("could not unsubscribe: Interaction Class Not Defined", e);
                    return;
                } catch (InteractionClassNotSubscribed e) {
                    logger.error("could not unsubscribe: Interaction Class Not Subscribed", e);
                    return;
                } catch (Exception e) {
                    logger.error(e);
                    CpswtUtils.sleepDefault();
                }
            }
        }

        _isSubscribed = false;
        logger.debug("unsubscribe: {}", get_class_name());
    }

    /**
    * Return true if "handle" is equal to the handle (RTI assigned) of this class
    * (that is, the ReceiveFromEP interaction class).
    *
    * @param handle handle to compare to the value of the handle (RTI assigned) of
    * this class (the ReceiveFromEP interaction class).
    * @return "true" if "handle" matches the value of the handle of this class
    * (that is, the ReceiveFromEP interaction class).
    */
    public static boolean match(int handle) {
        return handle == get_handle();
    }

    /**
    * Returns the handle (RTI assigned) of this instance's interaction class .
    *
    * @return the handle (RTI assigned) if this instance's interaction class
    */
    public int getClassHandle() {
        return get_handle();
    }

    /**
    * Returns the fully-qualified (dot-delimited) name of this instance's interaction class.
    *
    * @return the fully-qualified (dot-delimited) name of this instance's interaction class
    */
    public String getClassName() {
        return get_class_name();
    }

    /**
    * Returns the simple name (last name in its fully-qualified dot-delimited name)
    * of this instance's interaction class.
    *
    * @return the simple name of this instance's interaction class
    */
    public String getSimpleClassName() {
        return get_simple_class_name();
    }

    /**
    * Returns a set containing the names of all of the non-hiddenparameters of an
    * interaction class instance.
    *
    * @return set containing the names of all of the parameters of an
    * interaction class instance
    */
    public Set< String > getParameterNames() {
        return get_parameter_names();
    }

    /**
    * Returns a set containing the names of all of the parameters of an
    * interaction class instance.
    *
    * @return set containing the names of all of the parameters of an
    * interaction class instance
    */
    public Set< String > getAllParameterNames() {
        return get_all_parameter_names();
    }

    @Override
    public String getParameterName(int datamemberHandle) {
        if (datamemberHandle == _Data_handle) return "Data";
        else if (datamemberHandle == _NumberOfVariable_handle) return "NumberOfVariable";
        else if (datamemberHandle == _SimID_handle) return "SimID";
        else if (datamemberHandle == _actualLogicalGenerationTime_handle) return "actualLogicalGenerationTime";
        else if (datamemberHandle == _federateFilter_handle) return "federateFilter";
        else if (datamemberHandle == _originFed_handle) return "originFed";
        else if (datamemberHandle == _sourceFed_handle) return "sourceFed";
        else return super.getParameterName(datamemberHandle);
    }

    /**
    * Publishes the interaction class of this instance of the class for a federate.
    *
    * @param rti handle to the Local RTI Component
    */
    public void publishInteraction(RTIambassador rti) {
        publish(rti);
    }

    /**
    * Unpublishes the interaction class of this instance of this class for a federate.
    *
    * @param rti handle to the Local RTI Component
    */
    public void unpublishInteraction(RTIambassador rti) {
        unpublish(rti);
    }

    /**
    * Subscribes a federate to the interaction class of this instance of this class.
    *
    * @param rti handle to the Local RTI Component
    */
    public void subscribeInteraction(RTIambassador rti) {
        subscribe(rti);
    }

    /**
    * Unsubscribes a federate from the interaction class of this instance of this class.
    *
    * @param rti handle to the Local RTI Component
    */
    public void unsubscribeInteraction(RTIambassador rti) {
        unsubscribe(rti);
    }

    @Override
    public String toString() {
        return getClass().getName() + "("
                + "Data:" + get_Data()
                + "," + "NumberOfVariable:" + get_NumberOfVariable()
                + "," + "SimID:" + get_SimID()
                + "," + "actualLogicalGenerationTime:" + get_actualLogicalGenerationTime()
                + "," + "federateFilter:" + get_federateFilter()
                + "," + "originFed:" + get_originFed()
                + "," + "sourceFed:" + get_sourceFed()
                + ")";
    }

    private String _Data = "";
    private String _NumberOfVariable = "";
    private int _SimID = 0;

    /**
    * Set the value of the "Data" parameter to "value" for this parameter.
    *
    * @param value the new value for the "Data" parameter
    */
    public void set_Data( String value ) {
        _Data = value;
    }

    /**
    * Returns the value of the "Data" parameter of this interaction.
    *
    * @return the value of the "Data" parameter
    */
    public String get_Data() {
        return _Data;
    }
    /**
    * Set the value of the "NumberOfVariable" parameter to "value" for this parameter.
    *
    * @param value the new value for the "NumberOfVariable" parameter
    */
    public void set_NumberOfVariable( String value ) {
        _NumberOfVariable = value;
    }

    /**
    * Returns the value of the "NumberOfVariable" parameter of this interaction.
    *
    * @return the value of the "NumberOfVariable" parameter
    */
    public String get_NumberOfVariable() {
        return _NumberOfVariable;
    }
    /**
    * Set the value of the "SimID" parameter to "value" for this parameter.
    *
    * @param value the new value for the "SimID" parameter
    */
    public void set_SimID( int value ) {
        _SimID = value;
    }

    /**
    * Returns the value of the "SimID" parameter of this interaction.
    *
    * @return the value of the "SimID" parameter
    */
    public int get_SimID() {
        return _SimID;
    }

    protected ReceiveFromEP( ReceivedInteraction datamemberMap, boolean initFlag ) {
        super( datamemberMap, false );
        if ( initFlag ) setParameters( datamemberMap );
    }

    protected ReceiveFromEP( ReceivedInteraction datamemberMap, LogicalTime logicalTime, boolean initFlag ) {
        super( datamemberMap, logicalTime, false );
        if ( initFlag ) setParameters( datamemberMap );
    }

    /**
    * Creates an instance of the ReceiveFromEP interaction class, using
    * "datamemberMap" to initialize its parameter values.
    * "datamemberMap" is usually acquired as an argument to an RTI federate
    * callback method, such as "receiveInteraction".
    *
    * @param datamemberMap data structure containing initial values for the
    * parameters of this new ReceiveFromEP interaction class instance
    */
    public ReceiveFromEP( ReceivedInteraction datamemberMap ) {
        this( datamemberMap, true );
    }

    /**
    * Like {@link #ReceiveFromEP( ReceivedInteraction datamemberMap )}, except this
    * new ReceiveFromEP interaction class instance is given a timestamp of
    * "logicalTime".
    *
    * @param datamemberMap data structure containing initial values for the
    * parameters of this new ReceiveFromEP interaction class instance
    * @param logicalTime timestamp for this new ReceiveFromEP interaction class
    * instance
    */
    public ReceiveFromEP( ReceivedInteraction datamemberMap, LogicalTime logicalTime ) {
        this( datamemberMap, logicalTime, true );
    }

    /**
    * Creates a new ReceiveFromEP interaction class instance that is a duplicate
    * of the instance referred to by ReceiveFromEP_var.
    *
    * @param ReceiveFromEP_var ReceiveFromEP interaction class instance of which
    * this newly created ReceiveFromEP interaction class instance will be a
    * duplicate
    */
    public ReceiveFromEP( ReceiveFromEP ReceiveFromEP_var ) {
        super( ReceiveFromEP_var );

        set_Data( ReceiveFromEP_var.get_Data() );
        set_NumberOfVariable( ReceiveFromEP_var.get_NumberOfVariable() );
        set_SimID( ReceiveFromEP_var.get_SimID() );
    }

    /**
    * Returns the value of the parameter whose name is "datamemberName"
    * for this interaction.
    *
    * @param datamemberName name of parameter whose value is to be
    * returned
    * @return value of the parameter whose name is "datamemberName"
    * for this interaction
    */
    public Object getParameter( String datamemberName ) {
        if ( "Data".equals(datamemberName) ) return get_Data();
        else if ( "NumberOfVariable".equals(datamemberName) ) return get_NumberOfVariable();
        else if ( "SimID".equals(datamemberName) ) return new Integer(get_SimID());
        else return super.getParameter( datamemberName );
    }

    protected boolean setParameterAux( String datamemberName, String val ) {
        boolean retval = true;
        if ( "Data".equals( datamemberName) ) set_Data( val );
        else if ( "NumberOfVariable".equals( datamemberName) ) set_NumberOfVariable( val );
        else if ( "SimID".equals( datamemberName) ) set_SimID( Integer.parseInt(val) );
        else retval = super.setParameterAux( datamemberName, val );

        return retval;
    }

    protected boolean setParameterAux( String datamemberName, Object val ) {
        boolean retval = true;
        if ( "Data".equals( datamemberName) ) set_Data( (String)val );
        else if ( "NumberOfVariable".equals( datamemberName) ) set_NumberOfVariable( (String)val );
        else if ( "SimID".equals( datamemberName) ) set_SimID( (Integer)val );
        else retval = super.setParameterAux( datamemberName, val );

        return retval;
    }

    public void copyFrom( Object object ) {
        super.copyFrom( object );
        if ( object instanceof ReceiveFromEP ) {
            ReceiveFromEP data = (ReceiveFromEP)object;
            _Data = data._Data;
            _NumberOfVariable = data._NumberOfVariable;
            _SimID = data._SimID;
        }
    }
}

