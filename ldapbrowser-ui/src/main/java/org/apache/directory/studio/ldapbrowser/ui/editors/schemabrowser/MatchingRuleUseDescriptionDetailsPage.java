/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */

package org.apache.directory.studio.ldapbrowser.ui.editors.schemabrowser;


import java.util.List;

import org.apache.directory.shared.ldap.schema.syntax.AttributeTypeDescription;
import org.apache.directory.shared.ldap.schema.syntax.MatchingRuleDescription;
import org.apache.directory.shared.ldap.schema.syntax.MatchingRuleUseDescription;
import org.apache.directory.studio.ldapbrowser.core.model.schema.Schema;
import org.apache.directory.studio.ldapbrowser.core.model.schema.SchemaUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;


public class MatchingRuleUseDescriptionDetailsPage extends SchemaDetailsPage
{

    /** The main section, contains oid, names and desc */
    private Section mainSection;

    /** The numeric oid field */
    private Text numericOidText;

    /** The name link */
    private Hyperlink nameLink;

    /** The description field */
    private Text descText;

    /** The flag section, contains obsolete */
    private Section flagSection;

    /** The obsolete field */
    private Label isObsoleteText;

    /** The applies section, contains links */
    private Section appliesSection;


    /**
     * Creates a new instance of MatchingRuleUseDescriptionDetailsPage.
     *
     * @param schemaPage the master schema page
     * @param toolkit the toolkit used to create controls
     */
    public MatchingRuleUseDescriptionDetailsPage( SchemaPage schemaPage, FormToolkit toolkit )
    {
        super( schemaPage, toolkit );
    }


    /**
     * {@inheritDoc}
     */
    public void createContents( final ScrolledForm detailForm )
    {

        this.detailForm = detailForm;
        detailForm.getBody().setLayout( new GridLayout() );

        // create main section
        mainSection = toolkit.createSection( detailForm.getBody(), SWT.NONE );
        mainSection.setText( "Details" );
        mainSection.marginWidth = 0;
        mainSection.marginHeight = 0;
        mainSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( mainSection );

        // create flag section
        flagSection = toolkit.createSection( detailForm.getBody(), SWT.NONE );
        flagSection.setText( "Flags" );
        flagSection.marginWidth = 0;
        flagSection.marginHeight = 0;
        flagSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( flagSection );

        // create flag content
        Composite flagClient = toolkit.createComposite( flagSection, SWT.WRAP );
        GridLayout flagLayout = new GridLayout();
        flagLayout.numColumns = 1;
        flagLayout.marginWidth = 0;
        flagLayout.marginHeight = 0;
        flagClient.setLayout( flagLayout );
        flagSection.setClient( flagClient );

        isObsoleteText = toolkit.createLabel( flagClient, "Obsolete", SWT.CHECK );
        isObsoleteText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        isObsoleteText.setEnabled( false );

        // create applies section
        appliesSection = toolkit.createSection( detailForm.getBody(), Section.TWISTIE );
        appliesSection.setText( "Applies" );
        appliesSection.marginWidth = 0;
        appliesSection.marginHeight = 0;
        appliesSection.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        toolkit.createCompositeSeparator( appliesSection );
        appliesSection.addExpansionListener( new ExpansionAdapter()
        {
            public void expansionStateChanged( ExpansionEvent e )
            {
                detailForm.reflow( true );
            }
        } );

        // create raw section
        super.createRawSection();
    }


    /**
     * {@inheritDoc}
     */
    public void setInput( Object input )
    {
        MatchingRuleUseDescription mrud = null;
        if ( input instanceof MatchingRuleUseDescription )
        {
            mrud = ( MatchingRuleUseDescription ) input;
        }

        // create main content
        this.createMainContent( mrud );

        // set flag
        isObsoleteText.setEnabled( mrud != null && mrud.isObsolete() );

        // create contents of dynamic sections
        this.createAppliesContents( mrud );
        super.createRawContents( mrud );

        this.detailForm.reflow( true );
    }


    /**
     * Creates the content of the main section. It is newly created
     * on every input change to ensure a proper layout of 
     * multilined descriptions. 
     *
     * @param mrud the matching rule use description
     */
    private void createMainContent( MatchingRuleUseDescription mrud )
    {
        // dispose old content
        if ( mainSection.getClient() != null )
        {
            mainSection.getClient().dispose();
        }

        // create new client
        Composite mainClient = toolkit.createComposite( mainSection, SWT.WRAP );
        GridLayout mainLayout = new GridLayout( 2, false );
        mainClient.setLayout( mainLayout );
        mainSection.setClient( mainClient );

        // create new content
        if ( mrud != null )
        {
            toolkit.createLabel( mainClient, "Numeric OID:", SWT.NONE );
            numericOidText = toolkit.createText( mainClient, getNonNullString( mrud.getNumericOid() ), SWT.NONE );
            numericOidText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
            numericOidText.setEditable( false );

            toolkit.createLabel( mainClient, "Matching rule names:", SWT.NONE );
            nameLink = toolkit.createHyperlink( mainClient, "", SWT.WRAP );
            nameLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
            nameLink.addHyperlinkListener( this );

            Schema schema = getSchema();
            MatchingRuleDescription mrd = schema.hasMatchingRuleDescription( mrud.getNumericOid() ) ? schema
                .getMatchingRuleDescription( mrud.getNumericOid() ) : null;
            nameLink
                .setText( getNonNullString( mrd != null ? SchemaUtils.toString( mrd ) : SchemaUtils.toString( mrud ) ) );
            nameLink.setHref( mrd );
            nameLink.setUnderlined( mrd != null );
            nameLink.setEnabled( mrd != null );

            toolkit.createLabel( mainClient, "Descripton:", SWT.NONE );
            descText = toolkit.createText( mainClient, getNonNullString( mrud.getDescription() ), SWT.WRAP | SWT.MULTI );
            GridData gd = new GridData( GridData.FILL_HORIZONTAL );
            gd.widthHint = detailForm.getForm().getSize().x - 100 - 60;
            descText.setLayoutData( gd );
            descText.setEditable( false );
        }

        mainSection.layout();
    }


    /**
     * Creates the content of the applies section. 
     * It is newly created on every input change because the content
     * of this section is dynamic.
     *
     * @param mrud the matching rule use description
     */
    private void createAppliesContents( MatchingRuleUseDescription mrud )
    {
        // dispose old content
        if ( appliesSection.getClient() != null )
        {
            appliesSection.getClient().dispose();
        }

        // create new client
        Composite appliesClient = toolkit.createComposite( appliesSection, SWT.WRAP );
        appliesClient.setLayout( new GridLayout() );
        appliesSection.setClient( appliesClient );

        // create content
        if ( mrud != null )
        {
            List<String> names = mrud.getApplicableAttributes();
            if ( names != null && !names.isEmpty() )
            {
                appliesSection.setText( "Applies (" + names.size() + ")" );
                Schema schema = getSchema();
                for ( String name : names )
                {
                    if ( schema.hasAttributeTypeDescription( name ) )
                    {
                        AttributeTypeDescription appliesAtd = schema.getAttributeTypeDescription( name );
                        Hyperlink appliesLink = toolkit.createHyperlink( appliesClient, SchemaUtils
                            .toString( appliesAtd ), SWT.WRAP );
                        appliesLink.setHref( appliesAtd );
                        appliesLink.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                        appliesLink.setUnderlined( true );
                        appliesLink.setEnabled( true );
                        appliesLink.addHyperlinkListener( this );
                    }
                    else
                    {
                        Hyperlink appliesLink = toolkit.createHyperlink( appliesClient, name, SWT.WRAP );
                        appliesLink.setHref( null );
                        appliesLink.setUnderlined( false );
                        appliesLink.setEnabled( false );
                    }
                }
            }
            else
            {
                appliesSection.setText( "Applies (0)" );
                Text usedFromText = toolkit.createText( appliesClient, getNonNullString( null ), SWT.NONE );
                usedFromText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
                usedFromText.setEditable( false );
            }
        }
        else
        {
            appliesSection.setText( "Applies" );
        }

        appliesSection.layout();
    }

}
