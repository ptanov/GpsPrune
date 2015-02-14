package tim.prune.edit;

import java.util.ArrayList;

/**
 * Class to hold a list of field edits
 */
public class FieldEditList
{
	private ArrayList _editList = new ArrayList();


	/**
	 * Add an edit to the list
	 * @param inEdit FieldEdit
	 */
	public void addEdit(FieldEdit inEdit)
	{
		if (inEdit != null)
			_editList.add(inEdit);
	}

	/**
	 * @return number of edits in list
	 */
	public int getNumEdits()
	{
		return _editList.size();
	}

	/**
	 * Get the edit at the specified index
	 * @param inIndex index to get, starting at 0
	 * @return FieldEdit
	 */
	public FieldEdit getEdit(int inIndex)
	{
		return (FieldEdit) _editList.get(inIndex);
	}
}
